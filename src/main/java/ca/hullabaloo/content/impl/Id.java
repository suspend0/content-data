package ca.hullabaloo.content.impl;

public final class Id {
  public final Class type;
  public final int id;

  public Id(Class type, int id) {
    this.type = type;
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Id) {
      Id that = (Id) o;

      return id == that.id && type.equals(that.type);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + id;
    return result;
  }
}
