package ca.hullabaloo.content.impl;

import ca.hullabaloo.content.api.IdSet;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ArrayIdSetTest {
  @Test
  public void testAnd() throws Exception {
    IdSet<String> a = ArrayIdSet.fromSorted(new int[]{1, 2, 3, 4, 5});
    IdSet<String> b = ArrayIdSet.fromSorted(new int[]{4, 5, 6, 7, 8});
    IdSet<String> c = ArrayIdSet.fromSorted(new int[]{4, 5});
    Assert.assertEquals(a.and(b), c);
    Assert.assertEquals(b.and(a), c);
  }
}
