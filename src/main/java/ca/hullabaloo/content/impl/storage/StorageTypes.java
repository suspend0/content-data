package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.WholeType;
import ca.hullabaloo.content.api.SchemaVersion;
import ca.hullabaloo.content.util.ImmutableHashInterner;
import ca.hullabaloo.content.util.InternSet;
import com.google.common.collect.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;

public class StorageTypes {
  private static <E> E check(E r, Class<?> type) {
    checkArgument(r != null, "Unknown type", type);
    return r;
  }

  /**
   * These are only the properties owned (directly declared by) the entity
   */
  private ConcurrentMap<Class<?>, InternSet<String>> properties = Maps.newConcurrentMap();
  /**
   * The key is an entity, the value is a list of entities that the entity is composed from.
   * A type is always composed of at least itself.
   */
  private ConcurrentMap<Class<?>, InternSet<Class<?>>> composedOf = Maps.newConcurrentMap();

  public synchronized void register(final Class<?> type) {
    checkArgument(type.isInterface(), "Entities must be interface types", type);
    checkArgument(!type.isAnnotation(), "Entities must not be annotation types", type);
    checkArgument(type.isAnnotationPresent(SchemaVersion.class),
        "Entities must have a %s annotation", SchemaVersion.class.getSimpleName(), type);
    checkArgument(type.isAnnotationPresent(WholeType.class),
        "Entities must have a %s annotation", WholeType.class.getSimpleName(), type);
    if (properties.containsKey(type)) {
      return;
    }

    // Check all public methods, no matter where from
    for (Method method : type.getMethods()) {
      if (method.getDeclaringClass() != Object.class && method.getDeclaringClass() != Annotation.class) {
        checkArgument(method.getParameterTypes().length == 0, "only zero-argument getters are supported", method);
      }
    }

    // Only directly-declared methods are considered the properties of the entity
    List<String> properties = Lists.newArrayList();
    for (Method method : type.getDeclaredMethods()) {
      properties.add(method.getName());
    }
    this.properties.put(type, ImmutableHashInterner.copyOf(properties));

    // This type is composed of all super interface which are themselves an Entity
    Set<Class<?>> composedOf = Sets.newLinkedHashSet();
    gatherInterfaces(composedOf, type);
    for (Iterator<Class<?>> iterator = composedOf.iterator(); iterator.hasNext(); ) {
      Class<?> possible = iterator.next();
      if (possible.isAnnotationPresent(WholeType.class)) {
        register(possible);
      } else {
        iterator.remove();
      }
    }
    this.composedOf.put(type, ImmutableHashInterner.copyOf(composedOf));
  }

  private void gatherInterfaces(Set<Class<?>> result, Class<?> iface) {
    if (result.add(iface)) {
      for (Class<?> t : iface.getInterfaces()) {
        gatherInterfaces(result, t);
      }
    }
  }

  public InternSet<String> properties(Class<?> type) {
    return check(this.properties.get(type), type);
  }

  public InternSet<Class<?>> componentsOf(Class<?> type) {
    return check(this.composedOf.get(type), type);
  }

  /**
   * Takes (whole-type to fields) and returns (fraction-type to fields)
   */
  public <T> Multimap<Class<?>, String> fractionate(Multimap<Class<T>, String> fields) {
    // TODO: this doesn't restrict by fields
    ImmutableMultimap.Builder<Class<?>, String> r = ImmutableMultimap.builder();
    for (Map.Entry<Class<T>, String> source : fields.entries()) {
      for (Map.Entry<Class<?>, InternSet<Class<?>>> target : this.composedOf.entrySet()) {
        if (target.getValue().intern(source.getKey()) != null) {
          r.put(target.getKey(), "*");
        }
      }
    }
    return r.build();
  }
}
