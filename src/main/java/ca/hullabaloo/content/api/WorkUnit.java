package ca.hullabaloo.content.api;

public interface WorkUnit {
  <T> Updater<T> updater(Class<T> type);

  WorkUnit add(Update update);

  void commit();
}
