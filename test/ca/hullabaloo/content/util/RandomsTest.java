package ca.hullabaloo.content.util;

import com.google.common.collect.Sets;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;

public class RandomsTest {
  @Test
  public void sameSeedSameResult() throws Exception {
    for (int i = -133234343; i < 34343339; i += 3883) {
      int r1 = Randoms.multiplyCarry(i);
      int r2 = Randoms.multiplyCarry(i);
      // System.out.printf("%12d => %12d\n", i,r1);
      Assert.assertEquals(r1, r2);
    }
  }

  @Test
  public void strings() {
    Set<String> v = Sets.newHashSet();
    for (int i = 0; i < 128; i++) {
      String string = Randoms.string(80);
      Assert.assertEquals(string.length(), 80);
      Assert.assertTrue(v.add(string));
    }
  }
}
