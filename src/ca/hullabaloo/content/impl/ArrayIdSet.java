package ca.hullabaloo.content.impl;

import ca.hullabaloo.content.api.IdIterator;
import ca.hullabaloo.content.api.IdSet;
import ca.hullabaloo.content.impl.query.IdSetBuilder;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkArgument;

public class ArrayIdSet<T> implements IdSet<T> {
  public static <T> IdSet<T> of(int... values) {
    values = values.clone();
    Arrays.sort(values);
    return ofSorted(values);
  }

  public static <T> IdSet<T> ofSorted(int... values) {
    checkArgument(Ordering.natural().isStrictlyOrdered(Ints.asList(values)));
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
  public IdSet<T> andNot(IdSet<T> other) {
    // todo: better impl
    ArrayIdSet<T> that = (ArrayIdSet<T>) other;
    HashSet<Integer> copyThis = new HashSet<Integer>();
    copyThis.addAll(Ints.asList(this.values));
    HashSet<Integer> copyThat = new HashSet<Integer>();
    copyThat.addAll(Ints.asList(that.values));
    copyThis.removeAll(copyThat);
    int[] temp = Ints.toArray(copyThis);
    Arrays.sort(temp);
    return ArrayIdSet.ofSorted(temp);
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
  public IdIterator iterator() {
    return new Iter();
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

  private class Iter implements IdIterator {
    int pos = 0;

    @Override
    public boolean hasNext() {
      return pos < values.length;
    }

    @Override
    public int peek() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return values[pos];
    }

    @Override
    public int next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return values[pos++];
    }
  }
}
