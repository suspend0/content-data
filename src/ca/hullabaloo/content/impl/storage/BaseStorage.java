package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.*;
import ca.hullabaloo.content.impl.query.DataGatherer;
import ca.hullabaloo.content.impl.query.StandardQuery;
import com.google.common.eventbus.EventBus;

abstract class BaseStorage implements Storage {
  private volatile DataGatherer data;

  protected abstract EventBus eventBus();

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
    return new StandardQuery<T>(data(), resultType);
  }

  @Override
  public final WorkUnit begin() {
    return new WorkUnit(eventBus());
  }

  private DataGatherer data() {
    // TODO: un-hack. (spi() can return different values over time)
    if (data == null) {
      synchronized (this) {
        if (data == null) {
          data = new DataGatherer(spi());
          eventBus().register(data);
        }
      }
    }
    return data;
  }

}

