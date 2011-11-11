package ca.hullabaloo.content.impl.query;

import ca.hullabaloo.content.api.IdSet;

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

  private static class EmptyIdSet implements IdSet {
    @Override
    public IdSet and(IdSet other) {
      return this;
    }

    @Override
    public IdSet or(IdSet other) {
      return other;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean contains(int i) {
      return false;
    }
  }
}
