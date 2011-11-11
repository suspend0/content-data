package ca.hullabaloo.content.api;

public class Update {
  final Class<?> type;
  final int id;
  final String field;
  final String value;

  public Update(Class<?> type, int id, String field, String value) {
    this.type = type;
    this.id = id;
    this.field = field;
    this.value = value;
  }
}
