package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Storage;
import ca.hullabaloo.content.util.ImmutableHashInterner;
import com.google.common.collect.Interner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;

public class StorageTypes {
  // TODO an immutable tree of registered would be faster and take less space
  private ConcurrentMap<Class<?>, Integer> registered = Maps.newConcurrentMap();
  private ConcurrentMap<Class<?>, Interner<String>> properties = Maps.newConcurrentMap();
  private ConcurrentMap<Class<?>, int[]> subtypes = Maps.newConcurrentMap();

  public synchronized void register(Class<?> type) {
    checkArgument(type.isInterface());
    int id = type.getName().hashCode();
    checkArgument(!registered.values().contains(id));
    checkArgument(registered.putIfAbsent(type, id) == null);

    List<String> names = Lists.newArrayList();
    for (Method method : type.getMethods()) {
      if (!Storage.ID_METHOD_NAME.equals(method.getName())) {
        names.add(method.getName());
      }
    }
    this.properties.put(type, ImmutableHashInterner.create(names));

    int[] subs = {id};
    for (Map.Entry<Class<?>, int[]> entry : subtypes.entrySet()) {
      if (entry.getKey().isAssignableFrom(type)) {
        entry.setValue(append(entry.getValue(), id(type)));
      }
      if (type.isAssignableFrom(entry.getKey())) {
        subs = append(subs,id(entry.getKey()));
      }
    }
    subtypes.put(type, subs);
  }

  private int[] append(int[] list, int item) {
    if (!Ints.contains(list,item)) {
      list = Arrays.copyOf(list, list.length + 1);
      list[list.length-1] = item;
    }
    return list;
  }

  public Interner<String> properties(Class<?> type) {
    return this.properties.get(type);
  }

  public int id(Class<?> type) {
    // faster to recalculate, but safer to verify it's a type we know about 
    return registered.get(type);
  }

  public int[] ids(Class<?> type) {
    return subtypes.get(type).clone();
  }
}
