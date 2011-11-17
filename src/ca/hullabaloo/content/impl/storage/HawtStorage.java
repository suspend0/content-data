package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.RuntimeIOException;
import ca.hullabaloo.content.api.StorageSpi;
import ca.hullabaloo.content.util.SizeUnit;
import com.google.common.base.Function;
import com.google.common.collect.Interner;
import com.google.common.collect.Iterators;
import com.google.common.primitives.Ints;
import org.fusesource.hawtjournal.api.Journal;
import org.fusesource.hawtjournal.api.Location;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class HawtStorage extends BaseStorage {
  private final StorageTypes types = new StorageTypes();
  private final StorageSpi spi;

  public HawtStorage(File directory) {
    super();
    try {
      spi = new HawtStorageSpi(types, directory);
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }

  @Override
  protected StorageSpi spi() {
    return spi;
  }

  @Override
  protected StorageTypes storageTypes() {
    return types;
  }

  private static class HawtStorageSpi implements StorageSpi {
    private final StorageTypes types;
    private final Journal data;

    private final Function<Location, byte[]> reader = new Function<Location, byte[]>() {
      @Override
      public byte[] apply(Location input) {
        try {
          ByteBuffer buffer = HawtStorageSpi.this.data.read(input);
          if (buffer.hasArray()) {
            return buffer.array();
          }
          byte[] r = new byte[buffer.remaining()];
          buffer.get(r);
          return r;
        } catch (IOException e) {
          throw new RuntimeIOException(e);
        }
      }
    };

    public HawtStorageSpi(StorageTypes types, File directory) throws IOException {
      this.types = types;
      Journal journal = new Journal();
      journal.setDirectory(directory);
      journal.setArchiveFiles(false);
      journal.setChecksum(true);
      journal.setMaxFileLength(Ints.checkedCast(SizeUnit.MB.toBytes(1)));
      journal.setMaxWriteBatchSize(Ints.checkedCast(SizeUnit.KB.toBytes(10)));
      journal.open();
      this.data = journal;
    }

    @Override
    public Interner<String> properties(Class<?> type) {
      return types.properties(type);
    }

    @Override
    public Iterator<byte[]> data() {
      Iterator<Location> base = this.data.iterator();
      return Iterators.transform(base, reader);
    }

    @Override
    public void append(byte[] bytes) {
      try {
        this.data.write(ByteBuffer.wrap(bytes), true);
      } catch (IOException e) {
        throw new RuntimeIOException(e);
      }
    }

    @Override
    public int[] ids(Class<?> type) {
      return types.ids(type);
    }
  }
}
