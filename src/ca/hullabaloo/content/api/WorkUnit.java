package ca.hullabaloo.content.api;

import ca.hullabaloo.content.impl.BasicUpdater;
import ca.hullabaloo.content.impl.Update;
import ca.hullabaloo.content.impl.storage.Block;
import com.google.common.eventbus.EventBus;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.testng.collections.Lists;

import java.util.List;

public class WorkUnit {
  private final StorageSpi storage;
  private final EventBus events;
  private List<Update> updates = Lists.newArrayList();

  public WorkUnit(StorageSpi storage, EventBus events) {
    this.storage = storage;
    this.events = events;
  }

  public <T> Updater<T> updater(Class<T> type) {
    return new BasicUpdater<T>(this, type);
  }

  public void add(Update update) {
    updates.add(update);
  }

  public void commit() {
    Block.Writer<ByteArrayDataOutput> writer = Block.writer(ByteStreams.newDataOutput());
    for (Update update : updates) {
      writer.write(Storage.ID.apply(update.type), update.id, update.field, update.value);
    }
    byte[] bytes = writer.getOutput().toByteArray();
    storage.append(bytes);
    for (Update update : updates) {
      events.post(update);
    }
    this.updates = null;
  }
}
