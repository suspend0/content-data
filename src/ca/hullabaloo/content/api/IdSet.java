package ca.hullabaloo.content.api;

/**
 * IdSets are required to be immutable
 */
public interface IdSet<T> {
  int size();

  boolean contains(int i);

  IdSet<T> and(IdSet<T> other);

  IdSet<T> or(IdSet<T> other);
}
