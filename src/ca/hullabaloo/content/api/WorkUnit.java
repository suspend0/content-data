package ca.hullabaloo.content.api;

import ca.hullabaloo.content.impl.storage.Block;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class WorkUnit {
  private final StorageSpi storage;
  private Block.Writer<ByteArrayDataOutput> writer = Block.writer(ByteStreams.newDataOutput());

  public WorkUnit(StorageSpi storage) {
    this.storage = storage;
  }

  public <T> Updater<T> updater(Class<T> type) {
    return new Updater<T>(this,type);
  }

  public void add(Update update) {
    writer.write(Storage.ID.apply(update.type), update.id, update.field, update.value);
  }

  public void commit() {
    byte[] bytes = writer.getOutput().toByteArray();
    writer = null;
    storage.append(bytes);
  }
}
