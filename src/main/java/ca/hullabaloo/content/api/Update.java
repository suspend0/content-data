package ca.hullabaloo.content.api;

public class Update {
  public final String id;
  public final String field;
  public final String value;

  public Update(String id, String field, String value) {
    this.id = id;
    this.field = field;
    this.value = value;
  }
}
