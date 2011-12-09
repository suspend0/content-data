package ca.hullabaloo.content.impl.query;

import ca.hullabaloo.content.api.IdSet;
import ca.hullabaloo.content.impl.ArrayIdSet;
import com.google.common.primitives.Ints;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IdSetBuilder<T> {
  private final Set<Integer> delegate = new HashSet<Integer>();

  public IdSetBuilder() {
  }

  public boolean add(int id) {
    return delegate.add(id);
  }

  public boolean remove(int id) {
    return delegate.remove(id);
  }

  public IdSet<T> build() {
    int[] values = Ints.toArray(delegate);
    Arrays.sort(values);
    return ArrayIdSet.ofSorted(values);
  }
}
