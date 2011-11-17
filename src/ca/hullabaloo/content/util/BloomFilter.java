package ca.hullabaloo.content.util;

import com.google.common.primitives.Ints;

import java.util.BitSet;

import static com.google.common.base.Preconditions.checkArgument;

public class BloomFilter {
  private final BitSet bits;
  private final int bitCount;
  private final int k;

  public BloomFilter(int k, int expectedElements) {
    checkArgument(k <= 8);
    checkArgument(k > 0);
    this.k = k;
    this.bitCount = calculateSize(k, expectedElements);
    this.bits = new BitSet(bitCount);
  }

  private int calculateSize(long k, long n) {
    long sz = n * k;
    sz *= 1.44d;
    return Hashes.roundPowerOfTwo(Ints.checkedCast(sz)) << 1;
  }

  public void add(Object element) {
    int v = Hashes.smear(element.getClass().hashCode() ^ element.hashCode());
    bits.set(v & bitCount - 1);
    for (int i = k - 1; i != 0; --i) {
      v = Randoms.multiplyCarry(v);
      bits.set(v & bitCount - 1);
    }
  }

  /**
   * This has the normal bloom filter false positive rate
   */
  public boolean contains(Object element) {
    int v = Hashes.smear(element.getClass().hashCode() ^ element.hashCode());
    boolean r = bits.get(v & bitCount - 1);
    for (int i = k - 1; i != 0; i--) {
      v = Randoms.multiplyCarry(v);
      r &= bits.get(v & bitCount - 1);
    }
    return r;
  }
}
