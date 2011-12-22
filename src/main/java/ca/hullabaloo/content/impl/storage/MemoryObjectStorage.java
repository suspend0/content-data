package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Identified;
import ca.hullabaloo.content.api.ObjectStorageSpi;
import ca.hullabaloo.content.api.Storage;
import ca.hullabaloo.content.impl.Id;
import ca.hullabaloo.content.util.Guava;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.*;
import com.google.common.eventbus.Subscribe;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class MemoryObjectStorage implements ObjectStorageSpi {
  private final StorageTypes types;
  private final ByType data = new ByType();

  public MemoryObjectStorage(StorageTypes types) {
    this.types = types;
  }

  @Override
  public <V extends Identified> List<V> get(Class<V> resultType, List<String> keys) {
    List<V> results = Lists.newArrayList();

    Cache<Class, ImmutableSet<Class>> typeCache = CacheBuilder.newBuilder().concurrencyLevel(1).build(
        new CacheLoader<Class, ImmutableSet<Class>>() {
          @Override
          public ImmutableSet<Class> load(Class key) throws Exception {
            return ImmutableSet.copyOf(MemoryObjectStorage.this.types.properties(key).values());
          }
        });

    List<Id> ids = this.types.ids(keys);
    for (Map.Entry<String, Id> e : Guava.zip(keys, ids)) {
      Id id = e.getValue();

      ImmutableMap.Builder<String, Object> c = ImmutableMap.builder();
      c.put(Storage.ID_METHOD_NAME, e.getKey());

      for (Class type : typeCache.getUnchecked(id.type)) {
        ById a = data.byType.get(type);
        if (a == null) {
          continue;
        }
        ByField b = a.byId.get(id);
        if (b == null) {
          continue;
        }
        c.putAll(b.byField);
      }

      ImmutableMap<String, Object> v = c.build();
      if (v.size() == 1 /*just id*/) {
        results.add(null);
      } else {
        results.add(Values.make(resultType, v));
      }
    }
    return results;
  }

  @Subscribe
  public void update(UpdateBatch batch) {
    for (UpdateRecord update : batch) {
      ById a = data.getOrCreate(update.fractionType);
      ByField b = a.getOrCreate(new Id(update.wholeType, update.id));
      b.byField.put(update.field, update.value);
    }
  }

  private static class ByType {
    private final ConcurrentMap<Class, ById> byType = Maps.newConcurrentMap();

    public ById getOrCreate(Class type) {
      ById r = byType.get(type);
      if (r == null) {
        r = new ById();
        ById ex = byType.putIfAbsent(type, r);
        if (ex != null) {
          return ex;
        }
      }
      return r;
    }
  }

  private static class ById {
    private final ConcurrentMap<Id, ByField> byId = Maps.newConcurrentMap();

    public ByField getOrCreate(Id id) {
      ByField r = byId.get(id);
      if (r == null) {
        r = new ByField();
        ByField ex = byId.putIfAbsent(id, r);
        if (ex != null) {
          return ex;
        }
      }
      return r;
    }
  }

  private static class ByField {
    private final ConcurrentMap<String, Object> byField = new MapMaker().concurrencyLevel(1).makeMap();
  }
}
