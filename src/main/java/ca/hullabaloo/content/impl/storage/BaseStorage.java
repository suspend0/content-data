package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.*;
import ca.hullabaloo.content.impl.query.StandardQuery;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.eventbus.EventBus;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

class BaseStorage implements Storage {
  private final EventBus eventBus = new EventBus();
  private final StorageSpi spi;
  private StorageTypes storageTypes;

  protected BaseStorage(StorageTypes types, final LogStorageSpi log, final ObjectStorageSpi objects) {
    this.storageTypes = types;
    final Indexer indexes = new Indexer(log);
    this.spi = new StorageSpi() {
      @Override
      public Iterator<byte[]> data() {
        return log.data();
      }

      @Override
      public <T, V> Supplier<IdSet<T>> index(Class<T> type, String fieldName, Predicate<V> predicate) {
        return indexes.getIndex(type, fieldName, predicate);
      }
    };
    eventBus.register(checkNotNull(log, LogStorageSpi.class.getSimpleName()));
    eventBus.register(checkNotNull(objects, ObjectStorageSpi.class.getSimpleName()));
    eventBus.register(checkNotNull(indexes, Indexer.class.getSimpleName()));
  }

  // TODO: deprecated; should be injected into DefaultStorageTypes
  @Override
  public final <T extends Identified> void register(Class<T> type) {
    ((DefaultStorageTypes) storageTypes).register(type);
  }

  @Override
  public final <T> Loader<T> loader(Class<T> resultType) {
    return new ByIdLoader<T>(resultType, spi, storageTypes);
  }

  @Override
  public final <T> Query<T> query(Class<T> resultType) {
    return new StandardQuery<T>(spi, resultType);
  }

  @Override
  public final WorkUnit begin() {
    return new DefaultWorkUnit(eventBus, storageTypes);
  }
}

