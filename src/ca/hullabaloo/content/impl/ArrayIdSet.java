package ca.hullabaloo.content.impl;

import ca.hullabaloo.content.api.IdSet;
import ca.hullabaloo.content.impl.query.IdSetBuilder;

import java.util.Arrays;

public class ArrayIdSet<T> implements IdSet<T> {
  public static <T> IdSet<T> fromSorted(int[] values) {
    return new ArrayIdSet<T>(values);
  }

  private final int[] values;

  private ArrayIdSet(int[] values) {
    this.values = values;
  }

  @Override
  public IdSet<T> and(IdSet<T> other) {
    // todo: better impl
    ArrayIdSet<T> that = (ArrayIdSet<T>) other;
    IdSetBuilder<T> r = new IdSetBuilder<T>();
    for (int i : this.values) {
      if (that.contains(i)) {
        r.add(i);
      }
    }
    return r.build();
  }

  @Override
  public IdSet<T> or(IdSet<T> other) {
    // todo: better impl
    ArrayIdSet<T> that = (ArrayIdSet<T>) other;
    IdSetBuilder<T> r = new IdSetBuilder<T>();
    for (int i : this.values) {
      r.add(i);
    }
    for (int i : that.values) {
      r.add(i);
    }
    return r.build();
  }

  @Override
  public int size() {
    return this.values.length;
  }

  @Override
  public boolean contains(int i) {
    return Arrays.binarySearch(this.values, i) >= 0;
  }

  @Override
  public String toString() {
    return Arrays.toString(this.values);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.values);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ArrayIdSet) {
      ArrayIdSet<?> that = (ArrayIdSet<?>) obj;
      return Arrays.equals(this.values, that.values);
    } else if (obj instanceof IdSet) {
      throw new UnsupportedOperationException("todo");
    }
    return false;
  }
}
