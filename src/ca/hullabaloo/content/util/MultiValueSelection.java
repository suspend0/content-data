package ca.hullabaloo.content.util;

import com.google.common.base.Function;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * An algorithm which can select the "top N" values from an unbounded input, with the additional property
 * that the inbound stream may contain multiple versions of a {@code value} for a given {@code key}.
 */
public interface MultiValueSelection {
  /**
   * Selects top-n values.
   *
   * For example, if you're selecting the top 10 items by name, and one of the items changes its name, provide
   * a function {@param indexer} which maps the inbound {@code source} of <code>{'id','name'}</code> tuples
   * to the <code>id</code> and a {@code comparator} to order tuples by <code>name</code>.  Whew.
   * <p/>
   * Later versions override earlier versions.
   *
   * @param n       the number of values to select
   * @param buffer  a it about how large a buffer size to use.  Larger buffer sizes can absorb more changing values.
   * @param cmp     the comparator providing a total order over {@link T}
   * @param indexer a function which maps inbound sets to an identifier
   * @param source  the source tuples
   * @param <T>     the tuple type.
   * @return the top n values, sorted in increasing order.
   */
  <T> List<T> topN(int n, int buffer, Comparator<? super T> cmp, Function<? super T, ?> indexer, Iterator<? extends T> source);
}
