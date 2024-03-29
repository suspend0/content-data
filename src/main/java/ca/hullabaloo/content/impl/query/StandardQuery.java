package ca.hullabaloo.content.impl.query;

import ca.hullabaloo.content.api.IdSet;
import ca.hullabaloo.content.api.Query;
import ca.hullabaloo.content.api.StorageSpi;
import ca.hullabaloo.content.impl.storage.Values;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class StandardQuery<T> implements Query<T> {
  private final Class<T> type;
  private final T fields;
  private final Map<String, Predicate<?>> fieldValues = Maps.newHashMap();
  private String fieldName;
  private final StorageSpi storage;

  public StandardQuery(StorageSpi storage, Class<T> type) {
    this.storage = storage;
    this.type = type;
    this.fields = proxy(type);
  }

  @Override
  public T fields() {
    return fields;
  }

  @Override
  public <V> Query<T> withEquals(V fieldNameCall, V value, V... orValues) {
    checkArgument(fieldNameCall == null);
    checkState(this.fieldName != null);
    String fieldName = this.fieldName;
    this.fieldName = null;

    return withFieldEquals(fieldName, value, orValues);
  }

  private Query<T> withFieldEquals(String fieldName, Object value, Object... orValues) {
    checkNotNull(fieldName);
    checkNotNull(value);

    Predicate<Object> predicate;
    if (orValues.length == 0) {
      predicate = Predicates.equalTo(value);
    } else {
      List<Predicate<Object>> predicates = Lists.newArrayList();
      for (Object v : Lists.asList(value, orValues)) {
        predicates.add(Predicates.equalTo(v));
      }
      predicate = Predicates.or(predicates);
    }

    this.fieldValues.put(fieldName, predicate);
    return this;
  }

  @Override
  public IdSet<T> execute() {
    // TODO: hack -- no field values should mean return all values
    if (this.fieldValues.isEmpty()) {
      this.fieldValues.put("name", Predicates.<Object>alwaysTrue());
    }
    List<Supplier<IdSet<T>>> list = Lists.newArrayList();
    for (Map.Entry<String, Predicate<?>> restriction : fieldValues.entrySet()) {
      list.add(storage.index(this.type, restriction.getKey(), restriction.getValue()));
    }

    Iterator<Supplier<IdSet<T>>> ids = list.iterator();
    if (ids.hasNext()) {
      IdSet<T> r = ids.next().get();
      while (ids.hasNext()) {
        r = r.and(ids.next().get());
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
