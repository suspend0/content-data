package ca.hullabaloo.content.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MinMaxPriorityQueue;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

public class HeapMultiValueSelection implements MultiValueSelection {
  @Override
  public <T> List<T> topN(int n, int buffer, Comparator<? super T> cmp, Function<? super T, ?> indexer, Iterator<? extends T> source) {
    checkArgument(buffer >= n, "buffer must be at least large enough for items [%s < %s]", buffer, n);
    Map<Object, T> index = new HashMap<Object, T>(buffer);
    MinMaxPriorityQueue<T> pq = MinMaxPriorityQueue.orderedBy(cmp)
        .expectedSize(n + buffer + 1).create();

    while (source.hasNext()) {
      T item = source.next();
      T old = index.put(indexer.apply(item), item);
      if (old == null) {
        pq.add(item);
        if (pq.size() > buffer) {
          index.remove(indexer.apply(pq.removeLast()));
        }
      } else {
        pq.remove(old);
        pq.add(item);
        // if we replaced the old max with a new one then we may have already discarded values less than the new max
        // i.e., if we replace a 14 with a 17, and 17 is the new max, then we might have already seen and discarded a 16
        if (pq.size() == buffer && item == pq.peekLast()) {
          if (--buffer < n) {
            throw new IllegalArgumentException();
          }
          index.remove(indexer.apply(pq.removeLast()));
        }
      }
      assert pq.size() == index.size();
    }

    // pq.poll() b/c pq.toArray() or iterator() return elements in no particular order.
    List<T> r = Lists.newArrayListWithCapacity(n);
    T v;
    while (r.size() < n && null != (v = pq.poll())) {
      assert index.remove(indexer.apply(v)) != null;
      r.add(v);
    }
    return r;
  }
}
