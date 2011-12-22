package ca.hullabaloo.content.api;

import java.util.List;

public interface ObjectStorageSpi {
  public <V extends Identified> List<V> get(Class<V> type, List<String> keys);
}
