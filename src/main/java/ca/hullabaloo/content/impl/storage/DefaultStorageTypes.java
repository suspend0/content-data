package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Identified;
import ca.hullabaloo.content.api.Storage;
import ca.hullabaloo.content.api.Stored;
import ca.hullabaloo.content.impl.Id;
import ca.hullabaloo.content.impl.StoredAnnotation;
import com.google.common.collect.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;

public class DefaultStorageTypes implements StorageTypes {
  /**
   * These are only the properties owned (directly declared by) the entity
   */
  private final ConcurrentMap<Class<?>, Descriptor> properties = Maps.newConcurrentMap();
  
  private final Ids ids = new Ids();

  @Override
  public ImmutableMap<String, Class> properties(Class type) {
    return this.properties.get(type).reverse;
  }

  @Override
  public List<Id> ids(List<String> stringIds) {
    return this.ids.id(stringIds);
  }

  @Override
  public String id(Class type, int id) {
    return this.ids.id(type,id);
  }


  public synchronized <T extends Identified> void register(final Class<T> type) {
    String key = StoredAnnotation.getKey(type);
    Iterator<Class<?>> duplicateKey = Iterators.filter(properties.keySet().iterator(), StoredAnnotation.hasKey(key));
    checkArgument(!duplicateKey.hasNext(), "Duplicate types for key %s", key, type, Iterators.getOnlyElement(duplicateKey, null));

    // whew!
    registerInternal(type);
    ids.register(type);
  }

  private ImmutableSet<String> registerInternal(Class<?> type) {
    checkArgument(type.isInterface(), "Entities must be interface types", type);
    checkArgument(!type.isAnnotation(), "Entities must not be annotation types", type);
    checkArgument(type.isAnnotationPresent(Stored.class),
        "Entities must have a %s annotation", Stored.class.getSimpleName(), type);
    if (properties.containsKey(type)) {
      return properties.get(type).forward.get(type);
    }

    // Check all public methods, no matter where from
    for (Method method : type.getMethods()) {
      if (method.getDeclaringClass() != Object.class && method.getDeclaringClass() != Annotation.class) {
        checkArgument(method.getParameterTypes().length == 0, "only zero-argument getters are supported", method);
      }
    }

    ImmutableSetMultimap.Builder<Class, String> properties = ImmutableSetMultimap.builder();

    // Only directly-declared methods are considered the properties of the entity
    for (Method method : type.getDeclaredMethods()) {
      properties.put(type, method.getName());
    }

    // This type is composed of all super interface which are themselves an Entity
    Set<Class<?>> composedOf = Sets.newLinkedHashSet();
    gatherInterfaces(composedOf, type);
    for (Class<?> possible : composedOf) {
      if (possible != type && possible.isAnnotationPresent(Stored.class)) {
        checkArgument(StoredAnnotation.hasDefaultKey(possible),
            "No extension of %s types with key() except for fragmentation", Stored.class.getSimpleName());
        properties.putAll(possible, registerInternal(possible));
      }
    }

    ImmutableSetMultimap<Class, String> all = properties.build();
    validateAll(all);
    this.properties.put(type, new Descriptor(all));
    return all.get(type);
  }

  private void validateAll(ImmutableSetMultimap<Class, String> all) {
    for (Class<?> type : properties.keySet()) {
      try {
        type.getMethod(Storage.ID_METHOD_NAME);
      } catch (NoSuchMethodException e) {
        throw new IllegalArgumentException(String.format("Missing required method %s() on %s", Storage.ID_METHOD_NAME, type));
      }
    }

    if (all.values().size() > ImmutableSet.copyOf(all.values()).size()) {
      HashMultimap<String, Class> inverted = Multimaps.invertFrom(all, HashMultimap.<String, Class>create());
      List<String> duplicateMethods = Lists.newArrayList();
      for (Map.Entry<String, Collection<Class>> possible : inverted.asMap().entrySet()) {
        if (possible.getValue().size() > 0) {
          duplicateMethods.add(String.format("%s() on %s", possible.getKey(), possible.getValue()));
        }
      }
      throw new IllegalArgumentException(String.format("Method defined on multiple %s types: %s",
          Stored.class.getSimpleName(), duplicateMethods));
    }
  }

  private void gatherInterfaces(Set<Class<?>> result, Class<?> iface) {
    if (result.add(iface)) {
      for (Class<?> t : iface.getInterfaces()) {
        gatherInterfaces(result, t);
      }
    }
  }

  private static class Descriptor {
    private final ImmutableSetMultimap<Class, String> forward;
    private final ImmutableMap<String, Class> reverse;

    private Descriptor(ImmutableSetMultimap<Class, String> properties) {
      this.forward = properties;
      ImmutableMap.Builder<String, Class> reverse = ImmutableMap.builder();
      for (Map.Entry<Class, String> prop : forward.entries()) {
        reverse.put(prop.getValue(), prop.getKey());
      }
      this.reverse = reverse.build();
    }
  }

}
