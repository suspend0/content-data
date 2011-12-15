package ca.hullabaloo.content.util;

import javax.annotation.Nullable;

// not a java.util.Set
public interface InternSet<E> extends Iterable<E> {
  public @Nullable E intern(E element);
}
