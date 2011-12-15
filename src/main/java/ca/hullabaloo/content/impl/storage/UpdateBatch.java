package ca.hullabaloo.content.impl.storage;

import com.google.common.collect.Iterators;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.Iterator;
import java.util.List;

/**
 * @see DefaultWorkUnit
 */
public class UpdateBatch implements Iterable<UpdateRecord> {
  private final List<UpdateRecord> updates;

  public UpdateBatch(List<UpdateRecord> updates) {
    this.updates = updates;
  }

  @Override
  public Iterator<UpdateRecord> iterator() {
    return Iterators.unmodifiableIterator(updates.iterator());
  }

  byte[] bytes() {
    Block.Writer<ByteArrayDataOutput> writer = Block.writer(ByteStreams.newDataOutput());
    for (UpdateRecord u : updates) {
      writer.write(u.wholeType, u.id, u.fractionType, u.field, u.value);
    }
    return writer.getOutput().toByteArray();
  }
}
