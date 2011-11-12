package ca.hullabaloo.content.api;

public class Update {
  public final Class<?> type;
  public final int id;
  public final String field;
  public final String value;

  public Update(Class<?> type, int id, String field, String value) {
    this.type = type;
    this.id = id;
    this.field = field;
    this.value = value;
  }
}
