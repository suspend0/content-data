package ca.hullabaloo.content.impl;

import ca.hullabaloo.content.api.IdIterator;
import ca.hullabaloo.content.api.IdSet;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

public class ArrayIdSetTest {
  @Test
  public void testAnd() throws Exception {
    IdSet<String> a = ArrayIdSet.ofSorted(1, 2, 3, 4, 5);
    IdSet<String> b = ArrayIdSet.ofSorted(4, 5, 6, 7, 8);
    IdSet<String> c = ArrayIdSet.ofSorted(4, 5);
    Assert.assertEquals(a.and(b), c);
    Assert.assertEquals(b.and(a), c);
  }

  @Test
  public void testOr() throws Exception {
    IdSet<String> a = ArrayIdSet.ofSorted(1, 2, 3, 4, 5);
    IdSet<String> b = ArrayIdSet.ofSorted(4, 5, 6, 7, 8);
    IdSet<String> c = ArrayIdSet.ofSorted(1, 2, 3, 4, 5, 6, 7, 8);
    Assert.assertEquals(a.or(b), c);
    Assert.assertEquals(b.or(a), c);
  }

  @Test
  public void testIterator() {
    int[] expected = {9, 2, 8, 4, 5};
    IdSet<String> a = ArrayIdSet.of(expected);
    int[] actual = new int[expected.length];
    int pos = 0;
    for (IdIterator iter = a.iterator(); iter.hasNext(); ) {
      actual[pos++] = iter.next();
    }
    Arrays.sort(expected);
    Assert.assertEquals(actual, expected);
  }

}
