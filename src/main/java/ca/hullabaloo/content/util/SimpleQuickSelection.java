package ca.hullabaloo.content.util;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Basically a transliteration of "partition-based general selection algorithm" from
 * http://en.wikipedia.org/wiki/Selection_algorithm
 *
 * <b>BUGS:</b> This impl has some edge cases exposed by the random data generation.
 *
 * Requires the entire input to be memory-resident.  Complexity is <b>k O(n)</b> with
 * a <b>k</b> of approx 1.5 and poor upper-bound performance.
 */
public class SimpleQuickSelection implements Selection {
  @SuppressWarnings({"unchecked"})
  @Override
  public <T> List<T> topN(int n, Comparator<? super T> cmp, Iterator<? extends T> source) {
    T[] list = (T[]) Lists.newArrayList(source).toArray();
    int pos = select(n+1, cmp, list, 0, list.length - 1);
    list = Arrays.copyOf(list, pos);
    Arrays.sort(list);
    return Arrays.asList(list);
  }

  private <T> int select(int n, Comparator<? super T> cmp, T[] list, int left, int right) {
    while (true) {
      int pivotIndex = QuickSelection.pivotIndex(left, right);
      int pivotNewIndex = QuickSelection.partition(cmp, list, left, right, pivotIndex);
      int pivotDist = pivotNewIndex - left + 1;
      if (pivotDist == n) {
        return pivotNewIndex;
      } else if (n < pivotDist) {
        right = pivotNewIndex - 1;
      } else {
        n = n - pivotDist;
        left = pivotNewIndex + 1;
      }
    }
  }
}