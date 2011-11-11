package ca.hullabaloo.content.impl.query;

import ca.hullabaloo.content.api.IdSet;
import ca.hullabaloo.content.api.Query;
import ca.hullabaloo.content.impl.storage.Values;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class StandardQuery<T> implements Query<T> {
  private final DataGatherer data;
  private final Class<T> type;
  private final T fields;
  private final Map<String, Predicate<Object>> fieldValues = Maps.newHashMap();
  private String fieldName;

  public StandardQuery(DataGatherer data, Class<T> type) {
    this.data = data;
    this.type = type;
    this.fields = proxy(type);
  }

  @Override
  public T fields() {
    return fields;
  }

  @Override
  public <V> Query<T> withEquals(V fieldNameCall, V value) {
    checkArgument(fieldNameCall == null);
    checkState(this.fieldName != null);
    String fieldName = this.fieldName;
    this.fieldName = null;

    return withEqualsX(fieldName, value);
  }

  @Override
  public Query<T> withEqualsX(String fieldName, Object value) {
    checkNotNull(fieldName);
    checkNotNull(value);
    this.fieldValues.put(fieldName, Predicates.equalTo(value));
    return this;
  }

  @Override
  public IdSet<T> execute() {
    Iterator<Map.Entry<String, Supplier<IdSet<T>>>> ids =
        this.data.getAll(this.type, this.fieldValues).entrySet().iterator();
    if (ids.hasNext()) {
      IdSet<T> r = ids.next().getValue().get();
      while (ids.hasNext()) {
        r = r.and(ids.next().getValue().get());
      }
      return r;
    } else {
      return IdSets.empty();
    }
  }

  private T proxy(Class<T> type) {
    return Values.proxy(type, new Values.MethodCallback() {
      @Override
      public void called(String methodName) {
        StandardQuery.this.fieldName = methodName;
      }
    });
  }
}
