package ca.hullabaloo.content.api;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

import java.util.Iterator;

public interface StorageSpi extends LogStorageSpi {
  Iterator<byte[]> data(); //TODO add since-sequence
  <T, V> Supplier<IdSet<T>> index(Class<T> type, String fieldName, Predicate<V> predicate);
}
