package ca.hullabaloo.content.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SizeUnitTest {
  @Test
  public void testToBytes() throws Exception {
    Assert.assertEquals(SizeUnit.BYTE.toBytes(5), 5);
  }

  @Test
  public void testToKiloBytes() throws Exception {
    Assert.assertEquals(SizeUnit.BYTE.toKiloBytes(SizeUnit.KB.toBytes(5)), 5);
  }

  @Test
  public void testToMegabytes() throws Exception {
    Assert.assertEquals(SizeUnit.BYTE.toMegabytes(SizeUnit.MB.toBytes(5)), 5);
  }

  @Test
  public void testToGigabytes() throws Exception {
    Assert.assertEquals(SizeUnit.BYTE.toGigabytes(SizeUnit.GB.toBytes(5)), 5);
  }
}
