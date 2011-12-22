package ca.hullabaloo.content.api;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public interface StorageSpi extends LogStorageSpi, ObjectStorageSpi {
  <T, V> Supplier<IdSet<T>> index(Class<T> type, String fieldName, Predicate<V> predicate);
}
