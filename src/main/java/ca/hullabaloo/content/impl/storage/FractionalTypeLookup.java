package ca.hullabaloo.content.impl.storage;

public interface FractionalTypeLookup {
  Class fractionalType(Class wholeType, String fieldName);
}
