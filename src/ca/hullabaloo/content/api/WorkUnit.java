package ca.hullabaloo.content.api;

import ca.hullabaloo.content.impl.BasicUpdater;
import ca.hullabaloo.content.impl.storage.UpdateBatch;
import com.google.common.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class WorkUnit {
  private final EventBus events;
  private List<Update> updates = new ArrayList<Update>();

  public WorkUnit(EventBus events) {
    this.events = events;
  }

  public <T> Updater<T> updater(Class<T> type) {
    return new BasicUpdater<T>(this, type);
  }

  public WorkUnit add(Update update) {
    assert updates != null : "WorkUnit already committed";
    this.updates.add(update);
    return this;
  }

  public void commit() {
    List<Update> updates = this.updates;
    this.updates = null;
    this.events.post(new UpdateBatch(updates));
  }
}
