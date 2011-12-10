package ca.hullabaloo.content.api;

import ca.hullabaloo.content.util.InternSet;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;

import java.util.Iterator;

public interface StorageSpi {
  InternSet<Class<?>> componentsOf(Class<?> type);
  InternSet<String> properties(Class<?> type);
  Iterator<byte[]> data();
  <T, V> Supplier<IdSet<T>> index(Class<T> type, String fieldName, Predicate<V> predicate);

  @Deprecated
  <T> int[] ids(Multimap<Class<T>, String> fields);
}
