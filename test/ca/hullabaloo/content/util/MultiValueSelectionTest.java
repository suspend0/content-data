package ca.hullabaloo.content.util;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

/**
 *
 */
public class MultiValueSelectionTest {
  @DataProvider(name = "impl")
  private Object[][] implementations() {
    return new Object[][]{
//        {new QuickSelection()},
        {new HeapMultiValueSelection()},
//        {new SimpleQuickSelection()}
    };
  }

  private static Function<P, Integer> F = new Function<P, Integer>() {
    @Override
    public Integer apply(P input) {
      return input.id;
    }
  };

  private static class C implements Comparator<P> {
    int compares = 0;

    @Override
    public int compare(P o1, P o2) {
      ++compares;
      int r = Ints.compare(o1.value, o2.value);
      if (r == 0) {
        r = Ints.compare(o1.id, o2.id);
      }
      return r;
    }
  }

  @Test(dataProvider = "impl")
  public void hardList(MultiValueSelection s) {
    int[][] input = {{1, 10}, {2, 10}, {3, 11}, {4, 12}, {5, 13}, {6, 14}, {7, 15}, {8, 16}, {9, 17},
        {1, 13}, {1, 12}, {1, 11}, {1, 10}, {1, 9}, {1, 8}, {1, 7}, {1, 6}, {1, 5}, {1, 4}, {1, 3}, {1, 2}};

    doRun(s, "bad", 3, asList(input));
  }

  @Test(dataProvider = "impl")
  public void exerciseBufferDecrement(MultiValueSelection s) {
    int[][] input = {
        // fill the buffer with some values
        {1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}, {7, 7}, {8, 8}, {9, 9},
        {10, 10}, {11, 11}, {12, 12}, {13, 13}, {14, 14}, {15, 15}, {16, 16}, {16, 17}, {18, 18},
        // replace these values with greater.
        // When we process these, we've already thrown out [10,11,12], which are the values we want
        {1, 21}, {2, 22}, {3, 23}, {4, 24}, {5, 25}, {6, 26}, {7, 27}, {8, 28}, {9, 29},
    };

    try {
      s.topN(3, 10, new C(), F, asList(input).iterator());
      Assert.fail();
    } catch (IllegalArgumentException e) {
      // woot
    }

    List<P> actual = s.topN(3, 20, new C(), F, asList(input).iterator());
    List<P> expected = asList(new int[][]{{10, 10}, {11, 11}, {12, 12}});
    Assert.assertEquals(actual, expected);
  }

  @Test(dataProvider = "impl")
  public void shortList(MultiValueSelection s) {
    int[][] input = {{1, 1}, {1, 89}, {2, 32}, {2, 3}, {1, 42}};
    List<P> actual = s.topN(3, 6, new C(), F, asList(input).iterator());

    int[][] expected = {{2, 3}, {1, 42}};
    Assert.assertEquals(actual, asList(expected));
  }

  @Test(dataProvider = "impl")
  public void mediumList(MultiValueSelection s) {
    int[][] input = {{10, 1}, {2, 33}, {3, 353}, {23, 5992}, {4, 223},
        {3, 89}, {7, 32}, {98, 353}, {3, 2359}, {12, 3}, {19, 55}, {32, 59}, {16, 42}};
    List<P> actual = s.topN(3, 6, new C(), F, asList(input).iterator());

    int[][] expected = {{10, 1}, {12, 3}, {7, 32}};
    Assert.assertEquals(actual, asList(expected));
  }

  @Test(dataProvider = "impl")
  public void selectingLargePercentOfAMediumList(MultiValueSelection s) {
    int[][] input = {{389, 108}, {92, 421}, {2, 40}, {93, 199}, {347, 103}, {403, 111}, {284, 215},
        {376, 174}, {235, 190}, {23, 63}, {37, 353}, {116, 105}, {41, 129}, {116, 135}, {421, 168},
        {111, 288}, {275, 338}, {157, 123}, {194, 295}, {33, 270}, {85, 211}, {414, 429}, {22, 164},
        {263, 401}, {118, 363}, {375, 277}, {374, 84}, {219, 55}, {227, 428}, {389, 155}, {19, 93},
        {101, 268}, {149, 362}, {44, 247}, {354, 55}, {429, 87}, {340, 139}, {16, 407}, {85, 111},
        {208, 2}, {133, 35}, {363, 21}, {243, 358}};

    List<P> expected = bruteForceSort(24, asList(input));
    List<P> actual = s.topN(24, 48, new C(), F, asList(input).iterator());
    System.out.println("expected:" + expected);
    System.out.println("actual:" + actual);
    Assert.assertEquals(actual, expected);
  }

  @Test(dataProvider = "impl")
  public void twoItemListWithOneId(MultiValueSelection s) {
    int[][] input = {{6, 7}, {6, 11}};

    doRun(s, "smallProblem", 8, asList(input));
  }

  @Test(dataProvider = "impl")
  public void selectionCountGreaterThanInputLength(MultiValueSelection s) {
    int[][] input = {{758, 755}, {99, 471}, {144, 14}, {9, 442}, {580, 525}, {402, 555}, {485, 493},
        {22, 191}, {502, 407}, {113, 509}, {416, 663}, {616, 815}, {260, 370}, {810, 423}, {744, 599},
        {445, 466}, {52, 378}, {85, 610}, {592, 306}, {3, 432}, {410, 9}, {112, 855}, {532, 745}, {171, 631},
        {369, 410}, {137, 547}, {794, 534}, {233, 628}, {124, 361}, {29, 259}, {509, 449}, {323, 758},
        {562, 55}, {812, 140}, {554, 588}, {420, 560}, {379, 361}, {625, 787}, {159, 814}, {641, 127},
        {12, 17}, {69, 795}, {257, 605}, {162, 616}, {234, 51}, {836, 665}, {80, 446}, {26, 256}, {37, 471},
        {652, 681}, {252, 324}, {757, 351}, {117, 37}, {704, 553}, {363, 555}, {589, 299}, {854, 733},
        {128, 539}, {836, 109}, {130, 707}, {349, 811}, {726, 508}, {286, 569}, {630, 500}, {268, 760},
        {100, 815}, {257, 211}, {30, 533}, {735, 847}, {655, 436}, {161, 436}, {621, 253}, {575, 107},
        {469, 409}, {173, 47}, {536, 163}, {336, 340}, {706, 58}, {335, 9}, {810, 857}, {294, 388}, {812, 1},
        {21, 308}, {443, 209}, {118, 227}, {367, 254}};

    doRun(s, "problem", 199, asList(input));
  }

  @Test(dataProvider = "impl", enabled = false)
  public void largeList(MultiValueSelection s) {
    String label = "large";
    Random rand = new Random();
    for (int i = 1; i <= 10; i++) {
      int size = rand.nextInt(100000);
      int n = rand.nextInt(i * 50);
      List<P> items = new ArrayList<P>();
      while (items.size() < size) {
        items.add(new P(rand.nextInt(size), rand.nextInt(size * 10)));
      }
      Collections.shuffle(items);

      doRun(s, label, n, items);
    }
  }

  @Test(dataProvider = "impl")
  public void selectLargePercentage(MultiValueSelection s) {
    String label = "percent";
    Random rand = new Random();
    for (int i = 1; i <= 10; i++) {
      int size = rand.nextInt(1000);
      int n = (int) Math.max(0, size * 0.75);
      List<P> items = new ArrayList<P>();
      while (items.size() < size) {
        items.add(new P(rand.nextInt(size * 10), rand.nextInt(size * 10)));
      }
      Collections.shuffle(items);

      doRun(s, label, n, items);
    }
  }

  private List<P> doRun(MultiValueSelection s, String label, int n, List<P> items) {
    C cmp = new C();

    P[] input = Iterables.toArray(items, P.class);
    List<P> actual = s.topN(n, n * 4, cmp, F, items.iterator());
    List<P> expected = bruteForceSort(n, items);

    System.out.printf(s.getClass().getSimpleName() + "/%s: %s compares selecting %s from %s (%.2f)\n",
        label, cmp.compares, n, items.size(), (cmp.compares / (double) items.size()));
    Assert.assertEquals(actual, expected, Arrays.toString(input));
    return actual;
  }

  private List<P> bruteForceSort(int n, List<P> items) {
    Map<Object, P> index = Maps.newHashMap();
    for (P item : items) {
      index.put(F.apply(item), item);
    }
    List<P> expected = Lists.newArrayList(index.values());
    Collections.sort(expected, new C());
    expected = expected.subList(0, Math.min(n, index.size()));
    return expected;
  }

  private static List<P> asList(int[][] items) {
    List<P> r = Lists.newArrayList();
    for (int[] item : items) {
      P p = new P(item[0], item[1]);
      r.add(p);
    }
    return r;
  }

  private static class P {
    private final int id;
    private final int value;

    public P(int id, int value) {
      this.id = id;
      this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof P) {
        P that = (P) obj;
        return this.id == that.id && this.value == that.value;
      }
      return false;
    }

    @Override
    public String toString() {
      return "{" + id + "," + value + "}";
    }
  }
}
