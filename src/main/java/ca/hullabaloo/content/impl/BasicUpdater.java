package ca.hullabaloo.content.impl;

import ca.hullabaloo.content.api.Update;
import ca.hullabaloo.content.api.Updater;
import ca.hullabaloo.content.api.WorkUnit;
import ca.hullabaloo.content.impl.storage.Values;

import static com.google.common.base.Preconditions.checkArgument;

public class BasicUpdater<T> implements Updater<T> {
  private final WorkUnit c;
  private final T fields;

  private final Class<T> type;
  private int id;
  private String field;

  public BasicUpdater(WorkUnit c, Class<T> type) {
    this.c = c;
    this.type = type;
    this.fields = proxy(type);
  }

  @Override
  public T fields() {
    return fields;
  }

  @Override
  public Updater<T> forId(int id) {
    this.id = id;
    return this;
  }

  @Override
  public <V> Updater<T> set(V field, V value) {
    checkArgument(field == null && this.field != null);
    c.add(new Update(this.type, this.id, this.field, (String) value));
    this.field = null;
    return this;
  }

  @SuppressWarnings({"unchecked"})
  private T proxy(Class<T> type) {
    return Values.proxy(type, new Values.MethodCallback() {
      @Override
      public void called(String methodName) {
        BasicUpdater.this.field = methodName;
      }
    });
  }
}
