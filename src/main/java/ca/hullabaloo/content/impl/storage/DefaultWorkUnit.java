package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Update;
import ca.hullabaloo.content.api.Updater;
import ca.hullabaloo.content.api.WorkUnit;
import ca.hullabaloo.content.impl.Id;
import ca.hullabaloo.content.util.Guava;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultWorkUnit implements WorkUnit {
  private final EventBus events;
  private final StorageTypes types;
  private List<Update> updates = new ArrayList<Update>();

  public DefaultWorkUnit(EventBus events, StorageTypes types) {
    this.events = events;
    this.types = types;
  }

  @Override
  public <T> Updater<T> updater(Class<T> type) {
    return new BasicUpdater<T>(this, type);
  }

  @Override
  public WorkUnit add(Update update) {
    assert updates != null : "WorkUnit already committed";
    this.updates.add(update);
    return this;
  }

  @Override
  public void commit() {
    List<Update> updates = this.updates;
    this.updates = null;
    List<Id> ids = types.ids(Lists.transform(updates, ID));
    List<UpdateRecord> records = Lists.newArrayListWithExpectedSize(updates.size());
    for (Map.Entry<Id, Update> pair : Guava.zip(ids, updates)) {
      Id id = pair.getKey();
      Update u = pair.getValue();
      records.add(new UpdateRecord(id.type, id.id, types.properties(id.type).get(u.field), u.field, u.value));
    }
    this.events.post(new UpdateBatch(records));
  }

  private static final Function<Update, String> ID = new Function<Update, String>() {
    @Override
    public String apply(Update input) {
      return input.id;
    }
  };
}
