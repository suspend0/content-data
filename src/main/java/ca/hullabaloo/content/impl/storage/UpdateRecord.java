package ca.hullabaloo.content.impl.storage;

import static com.google.common.base.Preconditions.checkNotNull;

public class UpdateRecord {
  public final Class wholeType;
  public final int id;
  public final Class fractionType;
  public final String field;
  public final String value;

  public UpdateRecord(Class wholeType, int id, Class fractionType, String field, String value) {
    this.wholeType = checkNotNull(wholeType);
    this.id = checkNotNull(id);
    this.fractionType = checkNotNull(fractionType);
    this.field = checkNotNull(field);
    this.value = checkNotNull(value);
  }
  
  public String toString() {
    return String.format("Update{(%s,%s)(%s,%s)=%s}",
        wholeType.getSimpleName(), id, fractionType.getSimpleName(), field,value);
  }
}
