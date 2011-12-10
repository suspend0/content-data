package ca.hullabaloo.content.util;

// not a java.util.Set
public interface InternSet<E> extends Iterable<E> {
  public E intern(E element);
}
