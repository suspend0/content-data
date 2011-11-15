package ca.hullabaloo.content.util;

import com.google.common.collect.Lists;
import com.google.common.collect.MinMaxPriorityQueue;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Selects top N using a heap to maintain state.
 * <p/>
 * Very compact memory-wise but uses a lot of comparisons. Complexity is <b>O(n log n)</b>
 * the size of the input.
 */
public class HeapSelection implements Selection {
  @Override
  public <T> List<T> topN(int n, Comparator<? super T> cmp, Iterator<? extends T> source) {
    if (n == 0) {
      return Collections.emptyList();
    }
    MinMaxPriorityQueue<T> pq = MinMaxPriorityQueue.orderedBy(cmp)
        .expectedSize(n + 1).maximumSize(n).create();
    while (source.hasNext()) {
      pq.add(source.next());
    }
    // pq.toArray() or iterator() return elements in no particular order.
    List<T> r = Lists.newArrayListWithCapacity(n);
    T v;
    while (null != (v = pq.poll())) {
      r.add(v);
    }
    return r;
  }
}
