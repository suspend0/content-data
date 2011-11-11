package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Storage;
import ca.hullabaloo.content.util.ImmutableHashInterner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Interner;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;

public class StorageTypes {
  private ConcurrentMap<Integer, Class<?>> registered = new MapMaker().concurrencyLevel(1).makeMap();
  private volatile ImmutableMap<Class<?>, Interner<String>> properties = ImmutableMap.of();

  public synchronized void register(Class<?> type) {
    checkArgument(type.isInterface());
    checkArgument(registered.putIfAbsent(Storage.ID.apply(type), type) == null);

    List<String> names = Lists.newArrayList();
    for (Method method : type.getMethods()) {
      if (!Storage.ID_METHOD_NAME.equals(method.getName())) {
        names.add(method.getName());
      }
    }
    ImmutableMap.Builder<Class<?>, Interner<String>> properties = ImmutableMap.builder();
    properties.putAll(this.properties);
    properties.put(type, ImmutableHashInterner.create(names));
    this.properties = properties.build();

  }

  public Interner<String> properties(Class<?> type) {
    return this.properties.get(type);
  }
}
