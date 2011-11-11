package ca.hullabaloo.content.util;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static com.google.common.primitives.UnsignedBytes.toInt;

public class IntPack {
  static byte[][] LENGTH = new byte[256][4];

  static {
    for (int lenBits = 0; lenBits < 256; lenBits++) {
      LENGTH[lenBits] = new byte[4];
      for (int i = 3, b = lenBits; i >= 0; i--, b >>= 2) {
        LENGTH[lenBits][i] = (byte) ((b & 0x03) + 1);
      }
    }
  }

  public static byte[] pack(int... vals) {
    I in = new I(vals);
    P out = new P(vals.length);
    while (in.hasNext()) {
      int e1 = in.next();
      int e2 = in.next();
      int e3 = in.next();
      int e4 = in.next();
      out.append(e1, e2, e3, e4);
    }

    return out.toByteArray();
  }

  public static int[] unpack(byte[] bytes) {
    // We could make this faster by using sun.misc.Unsafe#getInt and masking the result
    U out = new U(bytes.length);
    int pos = 0;
    while (pos < bytes.length) {
      byte[] lengths = LENGTH[toInt(bytes[pos++])];
      for (int len : lengths) {
        int r = 0;
        switch (len) {
          case 4:
            r |= toInt(bytes[pos + 3]) << 24;
          case 3:
            r |= toInt(bytes[pos + 2]) << 16;
          case 2:
            r |= toInt(bytes[pos + 1]) << 8;
          case 1:
            r |= toInt(bytes[pos]);
            break;
          default:
            throw new Error();
        }
        pos += len;
        out.add(r);
      }
    }
    return out.toIntArray();
  }

  private static final class U {
    private int[] ints;
    private int pos;

    public U(int byteCount) {
      this.ints = new int[Math.min(4, byteCount / 2)];
    }

    public void add(int i) {
      if (pos == ints.length) {
        ints = Arrays.copyOf(ints, (ints.length + 1) * 3 / 2);
      }
      ints[pos++] = i;
    }

    public int[] toIntArray() {
      return Arrays.copyOf(ints, pos);
    }
  }

  private static final class P {
    private byte[] bytes;
    private int lenPos = 0;
    private int bytePos = 0;

    public P(int integers) {
      bytes = new byte[integers * 2];
    }

    public byte[] toByteArray() {
      return Arrays.copyOf(bytes, bytePos);
    }

    public void append(int e1, int e2, int e3, int e4) {
      while (bytePos + 17 > bytes.length) {
        bytes = Arrays.copyOf(bytes, (bytes.length + 1) * 3 / 2);
      }
      lenPos = bytePos;
      bytePos++;

      int len = 0;
      do {
        bytes[bytePos++] = (byte) e1;
        e1 >>>= 8;
        len++;
      } while (e1 != 0);
      --len;
      len <<= 2;

      do {
        bytes[bytePos++] = (byte) e2;
        e2 >>>= 8;
        len++;
      } while (e2 != 0);
      --len;
      len <<= 2;

      do {
        bytes[bytePos++] = (byte) e3;
        e3 >>>= 8;
        len++;
      } while (e3 != 0);
      --len;
      len <<= 2;

      do {
        bytes[bytePos++] = (byte) e4;
        e4 >>>= 8;
        len++;
      } while (e4 != 0);
      --len;

      bytes[lenPos] = (byte) len;
    }
  }

  private static final class I {
    private final int[] vals;
    private final int length;
    private int index = 0;

    public I(int[] vals) {
      this.vals = vals;
      this.length = (vals.length + 3) & ~0x03;
    }

    boolean hasNext() {
      return index < length;
    }

    int next() {
      if (index < vals.length) {
        return vals[index++];
      }
      if (index < length) {
        index++;
        return 0;
      }
      throw new NoSuchElementException();
    }
  }
}
