package ca.hullabaloo.content.util;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.primitives.Ints;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * An interner implemented using an open-access hash table (linear probing)
 */
public class ImmutableHashInterner<E> implements InternSet<E> {
  public static <E> InternSet<E> copyOf(E... items) {
    return copyOf(ImmutableSet.copyOf(items));
  }

  public static <E> InternSet<E> copyOf(Iterable<E> items) {
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

  // silly generic array creation
  @SuppressWarnings("unchecked")
  @Override
  public Iterator<E> iterator() {
    return Iterators.filter(Iterators.forArray(data), Predicates.notNull());
  }

  public String toString() {
    return Iterators.toString(iterator());
  }
  
  private final int mask;

  private final E[] data;

  private ImmutableHashInterner(ImmutableSet<E> items) {
    checkArgument(items.size() > 0);
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
