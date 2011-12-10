package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.RuntimeIOException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

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

    public void write(int type, int id, String field, String value) {
      try {
        out.writeByte(0);
        out.writeInt(type);
        out.writeInt(id);
        out.writeUTF(field);
        out.writeUTF(value);
      } catch (IOException e) {
        throw new RuntimeIOException(e);
      }
    }
  }

  public static class Reader {
    private enum State {READY, SET, COMPLETE}

    private final Iterator<byte[]> data;
    private State state;
    private ByteArrayDataInput chunk = ByteStreams.newDataInput(new byte[0]);

    public Reader(Iterator<byte[]> data) {
      this.data = data;
      readyState();
    }

    public int read(int[] type, Sink sink) {
      // also checks NPE
      if (type.length == 0) {
        return 0;
      }
      int count = 0;
      boolean more = true;
      while (more && advanceTo(type)) {
        more = sink.accept(chunk.readInt(), chunk.readUTF(), chunk.readUTF());
        readyState();
        count++;
      }
      return count;
    }

    public boolean advanceTo(int[] type) {
      while (true) {
        switch (state) {
          case READY:
            int t = chunk.readInt();
            if (Ints.contains(type, t)) {
              state = State.SET;
              return true;
            }
          case SET:
            chunk.skipBytes(4);
            chunk.skipBytes(chunk.readShort());
            chunk.skipBytes(chunk.readShort());
            readyState();
            continue;
          case COMPLETE:
            return false;
        }
      }
    }

    private void readyState() {
      state = (chunk.skipBytes(1) == 1 || advance()) ? State.READY : State.COMPLETE;
    }

    private boolean advance() {
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
    boolean accept(int id, String name, String value);
  }
}
