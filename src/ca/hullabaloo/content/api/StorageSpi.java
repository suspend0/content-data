package ca.hullabaloo.content.api;

import com.google.common.collect.Interner;

import java.util.Iterator;

public interface StorageSpi {
  int[] ids(Class<?> type);
  Interner<String> properties(Class<?> type);
  Iterator<byte[]> data();
}
