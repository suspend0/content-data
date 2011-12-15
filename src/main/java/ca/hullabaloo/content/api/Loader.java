package ca.hullabaloo.content.api;

import java.util.List;

public interface Loader<T> {
  List<T> getAll(String... ids);
}
