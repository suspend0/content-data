package ca.hullabaloo.content.api;

public interface Query<T> {
  public T fields();

  public <V> Query<T> withEquals(V fieldNameCall, V value, V... orValues);

  public IdSet<T> execute();
}
