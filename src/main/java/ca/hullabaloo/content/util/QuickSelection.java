package ca.hullabaloo.content.util;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Uses a variation of <b>Hoare's selection algorithm</b> (colloquially, <b>quickselect</b>) to
 * choose the top N items of a list.
 *
 * Empirically, performance is broadly <b>O(n)</b> the size of the input, plus <b>O(n log n)</b> the
 * size of the selection.  Therefore, selecting smaller segments out of larger inputs is most efficient.
 * This has poor upper-bound complexity selecting a large percentage of the inputs (approaching a
 * straight in-memory sort.)
 */
public class QuickSelection implements Selection {
  @Override
  public <T> List<T> topN(int n, Comparator<? super T> cmp, Iterator<? extends T> source) {
    if (n == 0) {
      return Collections.emptyList();
    }
    checkArgument(n > 0);

    @SuppressWarnings({"unchecked"})
    T[] buffer = (T[]) new Object[sizeBuffer(n)];

    // fill the buffer
    int pos = 0;
    while (pos < buffer.length && source.hasNext()) {
      buffer[pos++] = source.next();
    }

    // segment the buffer around a pivot, fill more of the buffer, and repeat
    while (source.hasNext()) {
      pos = 0;
      // Partition lower values to the first half of the buffer
      while (pos < n) {
        pos = partition(cmp, buffer, pos, buffer.length - 1, pivotIndex(pos, buffer.length));
      }

      // the item at p is the local maximum; fill rest of buffer with items less than it
      T m = buffer[pos];
      while (source.hasNext() && pos < buffer.length) {
        T candidate = source.next();
        if (cmp.compare(candidate, m) <= 0) {
          buffer[pos++] = candidate;
        }
      }
    }

    // everything up to the pivot position is less than the pivot, but unsorted.
    Arrays.sort(buffer, 0, pos, cmp);
    buffer = Arrays.copyOf(buffer, Math.min(n, pos));
    @SuppressWarnings({"unchecked"})
    List<T> r = Arrays.asList(buffer);
    return r;
  }

  private int sizeBuffer(int n) {
    // should be coordinated with 'pivot index'
    return n * 2;
  }

  static int pivotIndex(int low, int hi) {
    return low + (hi - low) / 2;
  }

  static <T> int partition(Comparator<? super T> cmp, T[] list, int left, int right, int pivotIndex) {
    checkArgument(list.length > 0);
    T pivotValue = list[pivotIndex];
    swap(list, pivotIndex, right);  // Move pivot to end
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (cmp.compare(list[i], pivotValue) < 0) {
        swap(list, storeIndex, i);
        ++storeIndex;
      }
    }
    swap(list, right, storeIndex);  // Move pivot to its final place
    return storeIndex;
  }

  private static void swap(Object x[], int a, int b) {
    Object t = x[a];
    x[a] = x[b];
    x[b] = t;
  }
}
