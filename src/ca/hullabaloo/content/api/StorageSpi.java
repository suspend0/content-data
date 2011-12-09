package ca.hullabaloo.content.api;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Interner;

import java.util.Iterator;

public interface StorageSpi {
  int[] ids(Class<?> type);
  Interner<String> properties(Class<?> type);
  Iterator<byte[]> data();
  <T, V> Supplier<IdSet<T>> index(Class<T> type, String fieldName, Predicate<V> predicate);
}
