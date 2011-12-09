package ca.hullabaloo.content.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Interner;
import com.google.common.primitives.Ints;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An interner implemented using an open-access hash table (linear probing)
 */
public class ImmutableHashInterner<E> implements Interner<E> {
  public static <E> Interner<E> create(E... items) {
    return create(ImmutableSet.copyOf(items));
  }

  public static <E> Interner<E> create(Iterable<E> items) {
    return new ImmutableHashInterner<E>(ImmutableSet.copyOf(items));
  }

  @Override
  public E intern(E sample) {
    if (sample == null) {
      return null;
    }
    for (int h = Hashes.smear(sample.hashCode()); true; h++) {
      int slot = h & mask;
      E candidate = data[slot];
      if (candidate == null) {
        return null;
      }
      if (candidate.equals(sample)) {
        return candidate;
      }
    }
  }

  private final int mask;
  private final E[] data;

  private ImmutableHashInterner(ImmutableSet<E> items) {
    int tableSize = chooseTableSize(items.size());
    Object[] data = new Object[tableSize];
    int mask = tableSize - 1;
    for (E item : items) {
      int hash = item.hashCode();
      for (int h = Hashes.smear(hash); ; h++) {
        int slot = h & mask;
        Object existing = data[slot];
        if (existing == null) {
          data[slot] = item;
          break;
        }
      }
    }
    this.data = cast(data);
    this.mask = mask;
  }

  private static int chooseTableSize(int setSize) {
    if (setSize < (1 << 29)) {
      return Integer.highestOneBit(setSize) << 2;
    }

    // The table can't be completely full or we'll get infinite reprobes
    checkArgument(setSize < Ints.MAX_POWER_OF_TWO, "collection too large");
    return Ints.MAX_POWER_OF_TWO;
  }

  @SuppressWarnings({"unchecked"})
  private E[] cast(Object[] data) {
    return (E[]) data;
  }
}
