package ca.hullabaloo.content.api;

import ca.hullabaloo.content.impl.storage.Values;

import static com.google.common.base.Preconditions.checkArgument;

public class Updater<T> {
  private final WorkUnit c;
  private final T fields;

  private final Class<T> type;
  private int id;
  private String field;

  public Updater(WorkUnit c, Class<T> type) {
    this.c = c;
    this.type = type;
    this.fields = proxy(type);
  }

  public T fields() {
    return fields;
  }

  public Updater<T> forId(int id) {
    this.id = id;
    return this;
  }

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
        Updater.this.field = methodName;
      }
    });
  }
}
