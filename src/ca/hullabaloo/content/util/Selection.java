package ca.hullabaloo.content.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public interface Selection {
  <T> List<T> topN(int n, Comparator<? super T> cmp, Iterator<? extends T> source);
}
