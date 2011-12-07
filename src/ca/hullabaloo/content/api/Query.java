package ca.hullabaloo.content.api;

public interface Query<T> {
  public T fields();

  public <V> Query<T> withEquals(V fieldNameCall, V value, V... orValues);

  public Query<T> withFieldEquals(String fieldName, Object value, Object... orValues);

  public IdSet<T> execute();
}
