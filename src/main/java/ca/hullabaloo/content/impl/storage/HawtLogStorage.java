package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.RuntimeIOException;
import ca.hullabaloo.content.api.LogStorageSpi;
import ca.hullabaloo.content.util.SizeUnit;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Ints;
import org.fusesource.hawtjournal.api.Journal;
import org.fusesource.hawtjournal.api.Location;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

class HawtLogStorage implements LogStorageSpi {
  private final Journal data;

  private final Function<Location, byte[]> reader = new Function<Location, byte[]>() {
    @Override
    public byte[] apply(Location input) {
      try {
        ByteBuffer buffer = HawtLogStorage.this.data.read(input);
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

  public HawtLogStorage(File directory) {
    Journal journal = new Journal();
    journal.setDirectory(directory);
    journal.setArchiveFiles(false);
    journal.setChecksum(true);
    journal.setMaxFileLength(Ints.checkedCast(SizeUnit.MB.toBytes(1)));
    journal.setMaxWriteBatchSize(Ints.checkedCast(SizeUnit.KB.toBytes(10)));
    try {
      journal.open();
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
    this.data = journal;
  }

  @Override
  public Iterator<byte[]> data() {
    Iterator<Location> base = this.data.iterator();
    return Iterators.transform(base, reader);
  }

  @Subscribe
  public void updates(UpdateBatch updates) {
    try {
      this.data.write(ByteBuffer.wrap(updates.bytes()), true);
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }
}
