package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.impl.Id;
import com.google.common.collect.ImmutableMap;

import java.util.List;

public interface StorageTypes {
  ImmutableMap<String, Class> properties(Class type);
  List<Id> ids(List<String> stringIds);
  String id(Class type, int id);
}
