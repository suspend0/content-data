package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.IdSet;
import ca.hullabaloo.content.api.StorageSpi;
import ca.hullabaloo.content.api.Update;
import ca.hullabaloo.content.util.Guava;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.SettableFuture;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class Indexer {
  private final ConcurrentLinkedQueue<IndexBuild> pendingBuilds = new ConcurrentLinkedQueue<IndexBuild>();
  private final ExecutorService collectionThread = Executors.newSingleThreadExecutor();
  private final Cache<Key, Supplier<IdSet>> indexes;
  private final StorageSpi storage;

  public Indexer(StorageSpi storage) {
    this.storage = storage;
    indexes = CacheBuilder.newBuilder()
        .concurrencyLevel(1)
        .build(new IndexCacheLoader());
  }

  public <T, V> Supplier<IdSet<T>> getIndex(Class<T> type, String fieldName, Predicate<V> predicate) {
    Key k = new Key(type, fieldName, predicate);
    return cast(indexes.getUnchecked(k));
  }

  @Subscribe
  public void updates(UpdateBatch updates) {
    // TODO: we may miss events here
    // during the index build process we have a future instead of the index.
    Iterable<Supplier<IdSet>> indexes = this.indexes.asMap().values();
    for (Index index : Iterables.filter(indexes, Index.class)) {
      index.update(updates);
    }
  }

  // we're careful to put only matching types in the map
  @SuppressWarnings("unchecked")
  private <T> Supplier<IdSet<T>> cast(Supplier unchecked) {
    return (Supplier<IdSet<T>>) unchecked;
  }

  private class IndexCacheLoader extends CacheLoader<Key, Supplier<IdSet>> {
    @Override
    public Supplier<IdSet> load(Key key) throws Exception {
      IndexBuild build = new IndexBuild(key);
      pendingBuilds.add(build);
      Supplier<Future<Index>> runWorkerOnce = Suppliers.memoize(build);
      return new ResultWaiter(runWorkerOnce);
    }
  }

  private class ResultWaiter implements Supplier<IdSet> {
    private final Supplier<Future<Index>> build;

    private ResultWaiter(Supplier<Future<Index>> build) {
      this.build = build;
    }

    @Override
    public IdSet get() {
      Supplier<Index> worker = Guava.supplier(build.get());
      Index index = worker.get();
      return index.get();
    }
  }

  // Index will enforce its type contract internally
  @SuppressWarnings("unchecked")
  private class IndexBuild implements Supplier<Future<Index>> {
    private final SettableFuture<Index> result = SettableFuture.create();
    private final Key key;
    private final Index index;

    public IndexBuild(Key key) {
      this.key = key;
      this.index = new Index(key.type, key.fieldName, key.predicate);
    }

    @Override
    public Future<Index> get() {
      collectionThread.execute(new Worker());
      return result;
    }

    public void complete() {
      indexes.asMap().put(key, index);
      result.set(index);
    }

    public void error(Exception e) {
      indexes.asMap().remove(key);
      result.setException(e);
    }
  }

  private class Worker implements Runnable {
    @Override
    public void run() {
      final ImmutableList<IndexBuild> work = getWorkForOneType();
      if (!work.isEmpty()) {
        try {
          final Class type = work.get(0).key.type;
          final List<Update> updates = Lists.newArrayList();

          Block.Reader reader = Block.reader(storage.data());
          reader.read(storage.ids(type), new Block.Sink() {
            @Override
            public boolean accept(int id, String fieldName, String value) {
              updates.add(new Update(type, id, fieldName, value));
              if (updates.size() >= 100) {
                UpdateBatch batch = new UpdateBatch(updates);
                for (IndexBuild build : work) {
                  build.index.update(batch);
                }
                updates.clear();
              }
              return true;
            }
          });

          for (IndexBuild build : work) {
            build.index.update(new UpdateBatch(updates));
            build.complete();
          }
        } catch (Exception e) {
          for (IndexBuild build : work) {
            build.error(e);
          }
        }
      }
    }

    private ImmutableList<IndexBuild> getWorkForOneType() {
      // Iterate+remove works b/c we're the only ones to remove from this queue
      IndexBuild first = pendingBuilds.poll();
      if (first == null) {
        return ImmutableList.of();
      }
      ImmutableList.Builder<IndexBuild> r = ImmutableList.builder();
      r.add(first);
      for (Iterator<IndexBuild> pending = pendingBuilds.iterator(); pending.hasNext(); ) {
        IndexBuild build = pending.next();
        if (build.key.type == first.key.type) {
          r.add(build);
          pending.remove();
        }
      }
      return r.build();
    }
  }

  private static final class Key {
    private final Class type;
    private final String fieldName;
    private final Predicate predicate;

    private Key(Class type, String fieldName, Predicate predicate) {
      this.predicate = predicate;
      this.fieldName = fieldName;
      this.type = type;
    }

    public boolean equals(Object o) {
      if (o instanceof Key) {
        Key that = (Key) o;
        return this.type == that.type
            && this.fieldName.equals(that.fieldName)
            && this.predicate.equals(that.predicate);
      }
      return false;
    }

    @Override
    public int hashCode() {
      int result = type.hashCode();
      result = 31 * result + fieldName.hashCode();
      result = 31 * result + predicate.hashCode();
      return result;
    }
  }
}