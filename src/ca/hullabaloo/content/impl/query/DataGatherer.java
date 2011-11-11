package ca.hullabaloo.content.impl.query;

import ca.hullabaloo.content.api.IdSet;
import ca.hullabaloo.content.api.Storage;
import ca.hullabaloo.content.api.StorageSpi;
import ca.hullabaloo.content.impl.ArrayIdSet;
import ca.hullabaloo.content.impl.Update;
import ca.hullabaloo.content.impl.storage.Block;
import ca.hullabaloo.content.util.Latch;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.*;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Ints;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class DataGatherer {
  private final StorageSpi storage;
  private final IndexMap indexes = new IndexMap();

  public DataGatherer(StorageSpi storage) {
    this.storage = storage;
  }

  @Subscribe
  public void handleUpdate(Update update) {
    for (Index index : indexes) {
      index.receive(update);
    }
  }

  /*
  every field<->predicate pair has an associated id set
    - together, an 'index'

  register an index for new events
  get a list of events from the storage provider
    - storage should abstract what's in the file system
    - v.s. new events
    - or better yet, events should ONLY post to others
        AFTER they have sync'd to disk
        INDEX after storage is durable
        so ORDER is consistent.

  LMAX DISRUPTOR
    - storage loop
    - index-update loop

  update events come in

  each index is pushed a copy of the event
  if the event matches field
    - and matches predicate, add to id set
    - and not matches, remove from id set
  on supplier get, we return the latest version of the id set
    - optimizations include storing side along edit lists that get applied

  record how many times
     - the index was used
     - how big the index is
     - how long it took to generate
     & use that to decide what to evict

  we should be able to do something with event ids
    - to enforce 'visibility'?
    - or maybe that's by design
    - what problem are we solving?  Consistent view?

   */

  public <T> Map<String, Supplier<IdSet<T>>> getAll(final Class<T> type, Map<String, Predicate<Object>> fieldValues) {
    final ImmutableMap.Builder<String, Supplier<IdSet<T>>> result = ImmutableMap.builder();
    final ImmutableMap<String, Index<T>> newIndexes;
    {
      final Interner<String> fieldNames = storage.properties(type);
      ImmutableMap.Builder<String, Index<T>> m = ImmutableMap.builder();
      for (Map.Entry<String, Predicate<Object>> entry : fieldValues.entrySet()) {
        String field = fieldNames.intern(entry.getKey());
        Predicate<Object> predicate = entry.getValue();
        Index<T> index = new Index<T>(type, field, predicate);
        Index<T> existing = indexes.putIfAbsent(index);
        if (existing == null) {
          m.put(field, index);
        } else {
          index = existing;
        }
        result.put(field, index);
      }
      newIndexes = m.build();
    }

    if (!newIndexes.isEmpty()) {
      Block.Reader reader = Block.reader(this.storage.data());
      reader.read(Storage.ID.apply(type), new Block.Sink() {
        @Override
        public boolean accept(int id, String name, String value) {
          Index<T> seeking;
          if (null != (seeking = newIndexes.get(name))) {
            seeking.accept(id, value);
          }
          return true;
        }
      });

      for (Index<T> index : newIndexes.values()) {
        index.build();
        index.ready.release();
      }
    }

    return result.build();
  }

  private static final class Index<T> implements Supplier<IdSet<T>> {
    private final Class<T> type;
    private final String fieldName;
    private final Predicate<Object> predicate;
    private final ConcurrentLinkedQueue<Integer> edits = new ConcurrentLinkedQueue<Integer>();
    private volatile IdSet<T> ids;
    private final Latch ready = new Latch();

    public Index(Class<T> type, String fieldName, Predicate<Object> predicate) {
      this.type = checkNotNull(type);
      this.fieldName = checkNotNull(fieldName);
      this.predicate = checkNotNull(predicate);
      this.ids = IdSets.empty();
    }

    @Override
    public IdSet<T> get() {
      return edits.isEmpty() ? this.ids : build();
    }

    private synchronized IdSet<T> build() {
      final Iterator<Integer> raw = Iterables.consumingIterable(edits).iterator();
      final PeekingIterator<Integer> peek = Iterators.peekingIterator(raw);
      Iterator<int[]> groups = new AbstractIterator<int[]>() {
        boolean positives = true;

        @Override
        protected int[] computeNext() {
          if (peek.hasNext()) {
            List<Integer> r = Lists.newArrayList();
            while (peek.hasNext() && peek.peek() >= 0 == positives) {
              r.add(Math.abs(peek.next()));
            }
            positives = !positives;
            return Ints.toArray(r);
          }
          return endOfData();
        }
      };

      IdSet<T> r = this.ids;
      boolean add = true;
      while (groups.hasNext()) {
        IdSet<T> group = ArrayIdSet.of(groups.next());
        if (add) {
          r = r.or(group);
        } else {
          r = r.andNot(group);
        }
        add = !add;
      }
      this.ids = r;
      return r;
    }

    public void receive(Update update) {
      //if the event matches field
      //  - and matches predicate, add to id set
      //  - and not matches, remove from id set
      if (this.type == update.type && this.fieldName.equals(update.field)) {
        ready.await();
        accept(update.id, update.value);
      }
    }

    private void accept(int id, String value) {
      if (this.predicate.apply(value)) {
        edits.add(id);
      } else {
        edits.add(-id);
      }
    }

    public int hashCode() {
      return fieldName.hashCode() + 31 * predicate.hashCode();
    }

    public boolean equals(Object o) {
      if (o instanceof Index) {
        Index that = (Index) o;
        return o == this ||
            (this.type.equals(that.type)
                && this.fieldName.equals(that.fieldName)
                && this.predicate.equals(that.predicate));
      }
      return false;
    }
  }

  @SuppressWarnings({"unchecked"})
  private static class IndexMap implements Iterable<Index> {
    private final ConcurrentMap<Index, Index> base = Maps.newConcurrentMap();

    @Override
    public Iterator<Index> iterator() {
      return base.keySet().iterator();
    }

    public <T> Index<T> putIfAbsent(Index<T> index) {
      return base.putIfAbsent(index, index);
    }
  }
}
