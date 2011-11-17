package ca.hullabaloo.content.util;

import com.google.common.collect.Lists;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class BloomFilterTest {
  @Test
  public void simple() {
    BloomFilter b = new BloomFilter(4, 1000);
    b.add("foo");
    Assert.assertTrue(b.contains("foo"));
    Assert.assertFalse(b.contains("bar"));
  }

  @Test(invocationCount = 100)
  public void medium() {
    String[] items = {
        "kvohpEhhDFjpgfFnAFnnnlhgvpDphhFpdblppoveFxxpjhFpoEFhhhoExphpFhnjDqFpfgEzploFeCDl",
        "AmFppDpfpjEFFoppgEpADnlpjmdjplFnewFhFpphppFppoFphhlBlnkFDplnppBopnxphohsFhFpFmBl",
        "khnpnovhzCFnpnlDlFpFlgtBkDppnFkxDgkppoFqhnpoFontmFlkxppFmaEEBlgEpoEFkFktprxzjpkF",
        "AFlEBDooBmxFpFpEEEjxfphofdxfphppFniDEBmFjhppppFFxgxFkplplphExBpFFppgrdpoEClEyDpg",
        "kvniDlnxpmrplDpgFozEeFwDnpADlFppnhflpitBFodphlopenoppoxlgFdkbfoEDnpFpnxyrDDExnkA",
        "AnppkypwkpmxFEFplnpoAFppafjphpCxpppppdFFpnhknFFjFhhFbFplFpxDEAxoFoFhhdFpEEDdlFpc",
        "kFFoFFpBdExBhoFpdhpnnllBEowplpoBppAslbhlpmbplopFpoEphyzlphlopsFnpolppopoxmyxoDrF",
        "AwpgxFgFdsExjlprppDnnlfpzpFolDBEgFnFhhixoDDxppoflmDmpFhxfgDCtlotpppFFnuFlopFxnpp",
        "kppizmpoDFpcFpAFnoFpnFhlEFFhppzhmFplyDgFpoFFolwFpvxzplfnppxnppppoEEdmnFjEEpFBbnA",
        "AgpnDpeFloFBpBqDpBpAnpphoFznjhoFhEDpgDhtgBpeEFlpkFlhFpeBFoFClhlfglFdpneFpppFEoyD",
    };

    run(Arrays.asList(items));
  }

  @Test(invocationCount = 100)
  public void large() {
    int count = 10884;
    List<String> items = Lists.newArrayList();
    for (int i = 0; i < count; i++) {
      String item = Randoms.string(80);
      items.add(item);
    }

    run(items);
  }

  private void run(List<String> items) {
    BloomFilter b = new BloomFilter(4, items.size());
    for (String item : items) {
      b.add(item);
      Assert.assertTrue(b.contains(item));
    }

    for (String item : items) {
      Assert.assertTrue(b.contains(item));
    }

    int falsePositives = 0;
    for (int i = 0; i < items.size(); i++) {
      String item = Randoms.string(80);
      if (!items.contains(item)) {
//        System.out.println(i + " " + item);
        if (b.contains(item)) {
          falsePositives++;
        }
      }
    }
    double f = falsePositives;
    double d = items.size();
    System.out.printf("false positives: %s of %s (%.4f)\n", falsePositives, items.size(), f/d);
    Assert.assertTrue(falsePositives == 1 || f/d < 0.01d, "rate: " + f/d);
  }
}
