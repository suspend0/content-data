package ca.hullabaloo.content.api;

public interface Updater<T> {
  T fields();

  Updater<T> forId(String id);

  <V> Updater<T> set(V field, V value);
}
