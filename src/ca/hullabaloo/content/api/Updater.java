package ca.hullabaloo.content.api;

public interface Updater<T> {
  T fields();

  Updater<T> forId(int id);

  <V> Updater<T> set(V field, V value);
}
