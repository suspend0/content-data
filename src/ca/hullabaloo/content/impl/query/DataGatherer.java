package ca.hullabaloo.content.impl.query;

import ca.hullabaloo.content.api.IdSet;
import ca.hullabaloo.content.api.Storage;
import ca.hullabaloo.content.api.StorageSpi;
import ca.hullabaloo.content.impl.storage.Block;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Interner;

import java.util.HashMap;
import java.util.Map;

public class DataGatherer {
  private final StorageSpi storage;

  public DataGatherer(StorageSpi storage) {
    this.storage = storage;
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
        ORDER is consistent.

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
    final ImmutableMap<String, Predicate<Object>> predicates;
    {
      final Interner<String> fieldNames = storage.properties(type);
      ImmutableMap.Builder<String, Predicate<Object>> m = ImmutableMap.builder();
      for (Map.Entry<String, Predicate<Object>> entry : fieldValues.entrySet()) {
        String field = fieldNames.intern(entry.getKey());
        Predicate<Object> predicate = entry.getValue();
        m.put(field, predicate);
      }
      predicates = m.build();
    }

    final Map<String, IdSetBuilder<T>> result = new HashMap<String, IdSetBuilder<T>>();
    Block.Reader reader = Block.reader(this.storage.data());
    reader.read(Storage.ID.apply(type), new Block.Sink() {
      @Override
      public boolean accept(int id, String name, String value) {
        Predicate<Object> seeking;
        if (null != (seeking = predicates.get(name))) {
          IdSetBuilder<T> ids = result.get(name);
          if (ids == null) {
            ids = new IdSetBuilder<T>();
            result.put(name, ids);
          }
          if (seeking.apply(value)) {
            ids.add(id);
          } else {
            // must always remove in case we saw an earlier version of the field matching the value
            ids.remove(id);
          }
        }
        return true;
      }
    });
    ImmutableMap.Builder<String, Supplier<IdSet<T>>> r = ImmutableMap.builder();
    for (Map.Entry<String, IdSetBuilder<T>> s : result.entrySet()) {
      r.put(s.getKey(), Suppliers.ofInstance(s.getValue().build()));
    }
    return r.build();
  }
}
