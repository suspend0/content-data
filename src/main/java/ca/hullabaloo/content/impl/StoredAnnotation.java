package ca.hullabaloo.content.impl;

import ca.hullabaloo.content.api.Stored;
import com.google.common.base.Predicate;

import static com.google.common.base.Preconditions.checkArgument;

@Stored(schemaVersion = -1)
public class StoredAnnotation {
  private static String defaultKey = StoredAnnotation.class.getAnnotation(Stored.class).key();

  static Predicate<Class<?>> hasKey() {
    return new Predicate<Class<?>>() {
      @Override
      public boolean apply(Class<?> input) {
        Stored inputAnnotation = input.getAnnotation(Stored.class);
        return inputAnnotation != null && !inputAnnotation.key().equals(defaultKey);
      }
    };
  }

  public static Predicate<Class<?>> hasKey(final String key) {
    return new Predicate<Class<?>>() {
      @Override
      public boolean apply(Class<?> input) {

        Stored inputAnnotation = input.getAnnotation(Stored.class);
        return inputAnnotation != null && key.equals(inputAnnotation.key());
      }
    };
  }

  public static String getKey(Class<?> type) {
    checkArgument(type.isAnnotationPresent(Stored.class),
        "Entities must have a %s annotation", Stored.class.getSimpleName(), type);

    String key = type.getAnnotation(Stored.class).key();
    checkArgument(!key.contains("-"), "Key must not contain reserved character '-'", type);
    checkArgument(!key.equals(StoredAnnotation.defaultKey),
        "Top level (whole) types must be defined with primary key", type);
    return key;
  }

  public static boolean hasDefaultKey(Class<?> possible) {
    return possible.getAnnotation(Stored.class).key().equals(StoredAnnotation.defaultKey);
  }
}
