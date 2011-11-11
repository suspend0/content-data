package ca.hullabaloo.content.impl.query;

import ca.hullabaloo.content.api.IdIterator;
import ca.hullabaloo.content.api.IdSet;

import java.util.NoSuchElementException;

public class IdSets {
  @SuppressWarnings({"unchecked"})
  public static <T> IdSet<T> empty() {
    return EMPTY;
  }

  public static <T> IdSet<T> empty(IdSet<T> ids) {
    if (ids.size() == 0) {
      return empty();
    }
    return ids;
  }

  private static final IdSet EMPTY = new EmptyIdSet();

  private static class EmptyIdSet implements IdSet, IdIterator {
    @Override
    public IdSet and(IdSet other) {
      return this;
    }

    @Override
    public IdSet andNot(IdSet other) {
      return this;
    }

    @Override
    public IdSet or(IdSet other) {
      return other;
    }

    @Override
    public IdIterator iterator() {
      return this;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean contains(int i) {
      return false;
    }

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public int peek() {
      throw new NoSuchElementException();
    }

    @Override
    public int next() {
      throw new NoSuchElementException();
    }
  }
}
