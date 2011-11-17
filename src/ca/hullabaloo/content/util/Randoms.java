package ca.hullabaloo.content.util;

import java.util.Random;

public class Randoms {
  /**
   * http://en.wikipedia.org/wiki/Multiply-with-carry
   */
  public static int multiplyCarry(long seed) {
    final long mask = 0xFFFFFFFFL;
    final long multiplier = 987654366;

    // carry = t / b (done in an unsigned way)
    long t = multiplier * (seed & mask);
    long div32 = t >>> 32; // div32 = t / (b+1)
    long carry = div32 + ((t & mask) >= mask - div32 ? 1L : 0L);
    // seeds[n] = (b-1)-t%b (done in an unsigned way)
    long result = 0xFFFFFFFEL - (t & mask) - (carry - div32 << 32) - carry & mask;
    return (int) result;
  }

  public static String string(final int length) {
    char[] c = new char[length];
    char[] a = Lazy.ALPHA;
    Random r = Lazy.RANDOM;
    for (int i = 0; i < c.length; i++) {
      c[i] = a[r.nextInt(a.length)];
    }
    return new String(c);
  }

  private static class Lazy {
    private static final Random RANDOM = new Random();
    private static final char[] ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHI".substring(0, 32).toCharArray();
  }
}