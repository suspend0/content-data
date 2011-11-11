package ca.hullabaloo.content.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;

public class IntPackTest {
  private static final boolean DEBUG = false;

  void print(String label, int v) {
    checkArgument(label.length() <= 8);
    System.out.printf("%8s, %32s %8s %s\n", label, Integer.toBinaryString(v), Integer.toHexString(v), v);
  }

  @Test
  public void googleNumbers() {
    int[] input = {1, 15, 511, 131071};
    runTest(input);
  }

  @Test
  public void negativeNumbers() {
    int[] input = {31, -18, -22, -3133434};
    runTest(input);
  }

  @Test
  public void longRun() {
    int[] input = {-1, 39, 931, 193, -3391349, -13, 1343, -1355, 88};
    runTest(input);
  }

  @Test(invocationCount = 10)
  public void randomRun() {
    Random rand = new Random();
    int[] input = new int[rand.nextInt(10000)];
    for (int i = 0; i < input.length; i++) {
      input[i] = rand.nextInt();
    }
    runTest(input);
  }

  private void runTest(int[] input) {
    byte[] bytes = IntPack.pack(input);
    if (DEBUG) {
      System.out.println("Length:" + bytes.length);
      for (byte b : bytes) {
        int i = (b & 0xFF) | (1 << 8);
        String s = Integer.toBinaryString(i);
        s = s.substring(1);
        System.out.print(s + " ");
      }
      System.out.println();
      for (byte b : bytes) {
        int i = (b & 0xFF) | (1 << 8);
        String s = Integer.toHexString(i);
        s = s.substring(1);
        System.out.print(s + " ");
      }
      System.out.println();
    }
    int[] r = IntPack.unpack(bytes);
    Assert.assertEquals(r.length, r.length & ~0x03);
    if (r.length > input.length) {
      input = Arrays.copyOf(input, r.length);
    }
    if (DEBUG) {
      System.out.println(Arrays.toString(r));
    }
    Assert.assertEquals(r, input);
  }

  @Test(enabled = DEBUG)
  public void bits() {
    print("max", Integer.MAX_VALUE);
    print("num", 481);
    print("num>>8", 481 & 0xFF);
    print("num>>8", 481 >> 8);
    print("-num+1", -481);
    print("-num>>8", -481 >>> 8);
  }

  @Test(enabled = DEBUG)
  public void lengths() {
    int i1 = Integer.parseInt("00000011", 2);
    int i2 = Integer.parseInt("00001100", 2);
    int i3 = Integer.parseInt("00110000", 2);
    int i4 = Integer.parseInt("11000000", 2);
    System.out.println(Integer.toHexString(i1) + " " + i2 + " " + i3 + " " + i4);
  }

  @Test(enabled = DEBUG)
  public void masks() {
    int v = 481;
    byte v1 = (byte) (481);
    byte v2 = 481 >> 8;

    print("v", v);
    print("v1", v1 & 0xFF);
    print("v2", v2);

    int r = v1 & 0xFF;
    r += (v2 & 0XFF) << 8;
    Assert.assertEquals(r, v);
  }

  @Test(enabled = DEBUG)
  public void shiftAndAdd() {
    int len = 0;
    len += 2;
    len -= 1;
    print("", len);

    len <<= 2;
    len += 4;
    len -= 1;
    print("", len);

    len <<= 2;
    len += 1;
    len -= 1;
    print("", len);

    len <<= 2;
    len += 3;
    len -= 1;
    print("", len);
  }

  @Test(enabled = DEBUG)
  public void byFour() {
    print("0x03", 0x03);
    print("17", 17 & 0x03);
    print("16", 16 & 0x03);
    print("15", 15 & 0x03);
  }

  @Test(enabled = DEBUG)
  public void decompose() {
    Random rand = new Random();
    int i = 0;
    int v = 13665;//rand.nextInt(20000);

    byte[] x = new byte[10];
    int pos = 0;
    do {
      print(i + ">", v);
      x[pos++] = (byte) v;
      System.out.println(Arrays.toString(x));
      v >>= 8;
    } while (v != 0);

    int r = 0;
    for (int b = 0; b < pos; b++) {
      r |= (x[b] & 0xFF) << (b * 8);
      print(i + "<", r);
    }
  }
}
