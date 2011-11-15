package ca.hullabaloo.content.util;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Uses a variation of <b>Hoare's selection algorithm</b> (colloquially, <b>quickselect</b>) to
 * choose the top N items of a list.
 *
 * Empirically, performance is broadly <b>O(n)</b> the size of the input, plus <b>O(n log n)</b> the
 * size of the selection.  Therefore, selecting smaller segments out of larger inputs is most efficient.
 * This has poor upper-bound complexity selecting a large percentage of the inputs.
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

  // =========================

  // 8, 9, 10, 11, 12, 13, 14, 15, 16, 17
  // 12, 13, 8, 9, 10, 11, 14, 15, 16, 17

  public static void main(String... args) {
    int[] list = new int[30];
    for (int i = 0; i < list.length; i++) {
      list[i] = 10 + i;
    }
    shuffle(list);
    int[] orig = list.clone();
    System.out.println("orig:" + Arrays.toString(list));
    System.out.println("pvt:" + list[list.length / 2]);
    int p = partition(list, 0, list.length - 1, list.length / 2);
    System.out.println("pass:" + Arrays.toString(list));
    System.out.println("part:" + p);

    list = orig;
    int[] top = topN(list, 4);
    System.out.println(Arrays.toString(top));
  }

  private static int[] topN(int[] list, int n) {
    String indexes = indexes(list);

    int run = 0;
    int p = list.length;
    System.out.println();
    System.out.println(indexes);
    System.out.println("run" + (run++) + ":" + Arrays.toString(list));
    do {
      System.out.printf("partition(list,0,%s,%s)\n", p - 1, p / 2);
      p = partition(list, 0, p - 1, p / 2);
      System.out.println("run" + (run++) + ":" + Arrays.toString(list));
    } while (p > n * 2);
    System.out.println("exit with p=" + p);

    Arrays.sort(list, 0, p);

    return Arrays.copyOf(list, n);
  }

  private static String indexes(int[] list) {
    list = list.clone();
    for (int i = 0; i < list.length; i++) {
      list[i] = i;
    }
    String str = Arrays.toString(list);
    str = str.replace('[', ' ');
    str = str.replaceAll(" (\\d),", " 0$1,");
    str = "[" + str.substring(1);
    return "idx :" + str;
  }

  private static int partition(int[] list, int left, int right, int pivotIndex) {
    int pivotValue = list[pivotIndex];
    swap(list, pivotIndex, right);  // Move pivot to end
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (list[i] < pivotValue) {
        swap(list, storeIndex, i);
        ++storeIndex;
      }
    }
    swap(list, right, storeIndex);  // Move pivot to its final place
    return storeIndex;
  }

  private static void shuffle(int[] list) {
    int size = list.length;
    Random rnd = new Random();
    for (int i = size; i > 1; i--) {
      swap(list, i - 1, rnd.nextInt(i));
    }
  }

  private static void swap(int x[], int a, int b) {
    int t = x[a];
    x[a] = x[b];
    x[b] = t;
  }
}
