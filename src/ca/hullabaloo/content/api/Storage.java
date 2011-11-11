package ca.hullabaloo.content.api;

import com.google.common.base.Function;

public interface Storage {
  String ID_METHOD_NAME = "id";

  public static Function<Class<?>, Integer> ID = new Function<Class<?>, Integer>() {
    public Integer apply(Class<?> type) {
      return type.getName().hashCode();
    }
  };

  void register(Class<?> type);

  <T> Loader<T> loader(Class<T> resultType);

  <T> Query<T> query(Class<T> resultType);

  WorkUnit begin();
}
