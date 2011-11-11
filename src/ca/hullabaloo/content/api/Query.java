package ca.hullabaloo.content.api;

public interface Query<T> {
  public T fields();

  public <V> Query<T> withEquals(V fieldNameCall, V value);

  public Query<T> withEqualsX(String fieldName, Object value);

  public IdSet<T> execute();
}
