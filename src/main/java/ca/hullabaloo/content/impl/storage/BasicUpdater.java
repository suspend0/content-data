package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.*;

import static com.google.common.base.Preconditions.checkArgument;

public class BasicUpdater<T> implements Updater<T> {
  private final WorkUnit c;
  private final T fields;

  private String id;
  private String field;

  public BasicUpdater(WorkUnit c, Class<T> type) {
    this.c = c;
    this.fields = proxy(type);
  }

  @Override
  public T fields() {
    return fields;
  }

  @Override
  public Updater<T> forId(String id) {
    this.id = id;
    return this;
  }

  @Override
  public <V> Updater<T> set(V field, V value) {
    checkArgument(field == null && this.field != null);
    c.add(new Update(this.id, this.field, (String) value));
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
