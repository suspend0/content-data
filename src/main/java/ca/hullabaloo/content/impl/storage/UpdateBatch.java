package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Storage;
import ca.hullabaloo.content.api.Update;
import com.google.common.collect.Iterators;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.Iterator;
import java.util.List;

/**
 * @see ca.hullabaloo.content.api.WorkUnit
 */
public class UpdateBatch implements Iterable<Update> {
  private final List<Update> updates;

  public UpdateBatch(List<Update> updates) {
    this.updates = updates;
  }

  @Override
  public Iterator<Update> iterator() {
    return Iterators.unmodifiableIterator(updates.iterator());
  }

  public byte[] bytes() {
    Block.Writer<ByteArrayDataOutput> writer = Block.writer(ByteStreams.newDataOutput());
    for (Update u : updates) {
      writer.write(Storage.ID.apply(u.type), u.id, u.field, u.value);
    }
    return writer.getOutput().toByteArray();
  }
}
