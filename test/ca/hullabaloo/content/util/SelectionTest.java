package ca.hullabaloo.content.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

/**
 *
 */
public class SelectionTest {
  @DataProvider(name = "impl")
  private Object[][] implementations() {
    return new Object[][]{
        {new QuickSelection()},
        {new HeapSelection()},
        {new SimpleQuickSelection()}
    };
  }

  @Test(dataProvider = "impl")
  public void shortList(Selection s) {
    int[] input = {1, 89, 32, 3, 42};
    List<Integer> actual = s.topN(3, Ordering.<Integer>natural(), Ints.asList(input).iterator());

    int[] expected = {1, 3, 32};
    Assert.assertEquals(actual, Ints.asList(expected));
  }

  @Test(dataProvider = "impl")
  public void mediumList(Selection s) {
    int[] input = {1, 33, 353, 5992, 223, 89, 32, 353, 2359, 3, 55, 59, 42};
    List<Integer> actual = s.topN(3, Ordering.<Integer>natural(), Ints.asList(input).iterator());

    int[] expected = {1, 3, 32};
    Assert.assertEquals(actual, Ints.asList(expected));
  }

  @Test(dataProvider = "impl")
  public void largeList(Selection s) {
    String label = "large";
    Random rand = new Random();
    for (int i = 1; i <= 10; i++) {
      int size = rand.nextInt(100000);
      int n = rand.nextInt(i * 50);
      List<Integer> items = new ArrayList<Integer>();
      while (items.size() < size) {
        items.add(rand.nextInt(size * 10));
      }
      Collections.shuffle(items);

      doRun(s, label, size, n, items);
    }
  }

  @Test(dataProvider = "impl")
  public void selectLargePercentage(Selection s) {
    String label = "percent";
    Random rand = new Random();
    for (int i = 1; i <= 10; i++) {
      int size = rand.nextInt(1000);
      int n = (int) Math.max(0, size * 0.75);
      List<Integer> items = new ArrayList<Integer>();
      while (items.size() < size) {
        items.add(rand.nextInt(size * 10));
      }
      Collections.shuffle(items);

      doRun(s, label, size, n, items);
    }
  }

  private void doRun(Selection s, String label, int size, int n, List<Integer> items) {
    class C implements Comparator<Integer> {
      int compares = 0;

      @Override
      public int compare(Integer o1, Integer o2) {
        compares++;
        return Ints.compare(o1, o2);
      }
    }

    C cmp = new C();

    int[] input = Ints.toArray(items);
    List<Integer> actual = s.topN(n, cmp, items.iterator());
    List<Integer> expected = Lists.newArrayList(items);
    Collections.sort(expected);
    expected = expected.subList(0, Math.min(n, size));

    System.out.printf(s.getClass().getSimpleName() + "/%s: %s compares selecting %s from %s (%.2f)\n",
        label, cmp.compares, n, size, (cmp.compares / (double) size));
    Assert.assertEquals(actual, expected, Arrays.toString(input));
  }
}
