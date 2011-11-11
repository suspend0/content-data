package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.*;
import ca.hullabaloo.content.impl.query.StandardQuery;

abstract class BaseStorage implements Storage {
  protected abstract StorageSpi spi();

  protected abstract StorageTypes storageTypes();

  @Override
  public final void register(Class<?> type) {
    storageTypes().register(type);
  }

  @Override
  public final <T> Loader<T> loader(Class<T> resultType) {
    return new ByIdLoader<T>(spi(), resultType);
  }

  @Override
  public final <T> Query<T> query(Class<T> resultType) {
    return new StandardQuery<T>(spi(), resultType);
  }

  @Override
  public final WorkUnit begin() {
    return new WorkUnit(spi());
  }

}

