package ca.hullabaloo.content.api;

import java.util.List;

public interface ObjectStorageSpi {
  public <V> List<V> get(Class<V> type, List<String> keys);
}
