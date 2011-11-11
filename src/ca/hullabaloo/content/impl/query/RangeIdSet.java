package ca.hullabaloo.content.impl.query;

import ca.hullabaloo.content.api.IdIterator;
import ca.hullabaloo.content.api.IdSet;

import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkArgument;

public class RangeIdSet<T> implements IdSet<T> {
  public static <T> IdSet<T> with(Class<T> type, int min, int max) {
    if (min == max) {
      return IdSets.empty();
    }
    return new RangeIdSet<T>(type, min, max);
  }

  private final Class<T> type;
  private final int min;
  private final int max;

  private RangeIdSet(Class<T> type, int min, int max) {
    checkArgument(min < max);
    this.type = type;
    this.min = min;
    this.max = max;
  }

  @Override
  public int size() {
    return max - min;
  }

  @Override
  public boolean contains(int i) {
    return i >= min && i < max;
  }

//  @Override
  public IdIterator iterator() {
    return new Iter(min, max);
  }

  @Override
  public IdSet<T> and(IdSet<T> other) {
    throw new UnsupportedOperationException("not yet");
  }

  @Override
  public IdSet<T> or(IdSet<T> other) {
    throw new UnsupportedOperationException("not yet");
  }

  private static class Iter implements IdIterator {
    private int val;
    private int max;

    public Iter(int min, int max) {
      this.val = min;
      this.max = max;
    }

    @Override
    public boolean hasNext() {
      return val < max;
    }

    @Override
    public int peek() {
      return val;
    }

    @Override
    public int next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return val++;
    }
  }
}
