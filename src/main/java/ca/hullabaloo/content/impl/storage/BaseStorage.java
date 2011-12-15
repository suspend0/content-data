package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.*;
import ca.hullabaloo.content.impl.query.StandardQuery;
import com.google.common.eventbus.EventBus;

abstract class BaseStorage implements Storage {
  protected abstract EventBus eventBus();

  protected abstract StorageSpi spi();

  // TODO: guice
  protected abstract DefaultStorageTypes storageTypes();

  @Override
  public final <T extends Identified> void register(Class<T> type) {
    storageTypes().register(type);
  }

  @Override
  public final <T> Loader<T> loader(Class<T> resultType) {
    return new ByIdLoader<T>(resultType, spi(), storageTypes());
  }

  @Override
  public final <T> Query<T> query(Class<T> resultType) {
    return new StandardQuery<T>(spi(), resultType);
  }

  @Override
  public final WorkUnit begin() {
    return new DefaultWorkUnit(eventBus(), storageTypes());
  }
}

