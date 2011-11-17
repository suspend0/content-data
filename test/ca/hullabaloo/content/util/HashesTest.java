package ca.hullabaloo.content.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HashesTest {
  @Test
  public void testRoundPowerOfTwo() throws Exception {
    Assert.assertEquals(Hashes.roundPowerOfTwo(8), 8);
    Assert.assertEquals(Hashes.roundPowerOfTwo(9), 16);
    Assert.assertEquals(Hashes.roundPowerOfTwo(6), 8);
  }
}
