package ca.hullabaloo.content.api;

import com.google.common.collect.Interner;

import java.util.Iterator;

public interface StorageSpi {
  Interner<String> properties(Class<?> type);
  Iterator<byte[]> data();
  public void append(byte[] data);
  int[] ids(Class<?> type);
}
