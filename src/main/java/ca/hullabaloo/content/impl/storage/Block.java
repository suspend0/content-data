package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.RuntimeIOException;
import ca.hullabaloo.content.util.See;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class Block {
  public static <O extends DataOutput> Writer<O> writer(O out) {
    return new Writer<O>(out);
  }

  public static Reader reader(Iterator<byte[]> data) {
    return new Reader(data);
  }

  public static class Writer<O extends DataOutput> {
    private final O out;

    public Writer(O out) {
      this.out = out;
    }

    public O getOutput() {
      return this.out;
    }

    @See(ref = ca.hullabaloo.content.api.Updater.class)
    public void write(Class whole, int id, Class fraction, String field, String value) {
      try {
        out.writeByte(0);
        out.writeByte(whole.getName().hashCode());
        out.writeByte(fraction.getName().hashCode());
        out.writeUTF(whole.getName());
        out.writeUTF(fraction.getName());
        out.writeInt(id);
        out.writeUTF(field);
        out.writeUTF(value);
      } catch (IOException e) {
        throw new RuntimeIOException(e);
      }
    }
  }

  public static class Reader {
    private static class TypeResolver {
      private static final Cache<String, Class> BY_NAME = CacheBuilder.newBuilder().concurrencyLevel(1)
          .build(new CacheLoader<String, Class>() {
            @Override
            public Class load(String key) throws Exception {
              return Class.forName(key);
            }
          });

      private final byte[] tokens;
      private final Multimap<Byte, Class> types;
      private Class whole;
      private Class fraction;

      public TypeResolver(Set<Class> typeSet) {
        tokens = new byte[typeSet.size()];
        types = HashMultimap.create();

        int i = 0;
        for (Class type : typeSet) {
          byte token = (byte) type.getName().hashCode();
          tokens[i++] = token;
          types.put(token, type);
        }
      }

      boolean acceptTokens(byte whole_t, byte fraction_t) {
        for (byte token : tokens) {
          if (token == whole_t || token == fraction_t) {
            return true;
          }
        }
        return false;
      }

      private Class acceptName(byte token, String name) {
        Iterable<Class> types = this.types.get(token);
        for (Class type : types) {
          if (name.equals(type.getName())) {
            return type;
          }
        }
        return null;
      }

      public boolean acceptNames(byte whole_t, String n1, byte fraction_t, String n2) {
        whole = acceptName(whole_t, n1);
        fraction = acceptName(fraction_t, n2);
        if (whole == null && fraction == null) {
          return false;
        }
        if (whole == null) {
          whole = BY_NAME.getUnchecked(n1);
        }
        if (fraction == null) {
          fraction = BY_NAME.getUnchecked(n2);
        }
        return true;
      }
    }

    private enum State {READY, SET, COMPLETE}

    private final Iterator<byte[]> data;
    private State state;
    private ByteArrayDataInput chunk = ByteStreams.newDataInput(new byte[0]);

    public Reader(Iterator<byte[]> data) {
      this.data = data;
      readyState();
    }

    public int read(ImmutableSet<Class> types, Sink sink) {
      // also checks NPE
      if (types.isEmpty()) {
        return 0;
      }
      TypeResolver resolver = new TypeResolver(types);
      int count = 0;
      boolean more = true;
      while (more && advanceTo(resolver)) {
        more = sink.accept(resolver.whole, chunk.readInt(), resolver.fraction, chunk.readUTF(), chunk.readUTF());
        readyState();
        count++;
      }
      return count;
    }

    public boolean advanceTo(ImmutableSet<Class> typeSet) {
      return advanceTo(new TypeResolver(typeSet));
    }

    public boolean advanceTo(TypeResolver types) {
      while (true) {
        switch (state) {
          case READY:
            byte whole_t = chunk.readByte();
            byte fraction_t = chunk.readByte();
            if (types.acceptTokens(whole_t, fraction_t)) {
              if (types.acceptNames(whole_t, chunk.readUTF(), fraction_t, chunk.readUTF())) {
                state = State.SET;
                return true;
              }
            } else {
              chunk.skipBytes(chunk.readShort()); // name
              chunk.skipBytes(chunk.readShort()); // name
            }
          case SET:
            chunk.skipBytes(4); // id
            chunk.skipBytes(chunk.readShort()); // field
            chunk.skipBytes(chunk.readShort()); // value
            readyState();
            continue;
          case COMPLETE:
            return false;
        }
      }
    }

    private void readyState() {
      // skipBytes(1) skips the zero-byte separator that's at the beginning of each record.
      // if we're at the end of our block, skipBytes() will return 0
      state = (chunk.skipBytes(1) == 1 || moreData()) ? State.READY : State.COMPLETE;
    }

    private boolean moreData() {
      while (data.hasNext()) {
        chunk = ByteStreams.newDataInput(data.next());
        if (chunk.skipBytes(1) == 1) {
          return true;
        }
      }
      return false;
    }
  }

  public interface Sink {
    boolean accept(Class whole, int id, Class fraction, String name, String value);
  }
}
