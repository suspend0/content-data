package ca.hullabaloo.content.api;

/**
 * The identity is the {@link ca.hullabaloo.content.api.WholeType#name()}
 * plus an incrementing integer.
 */
public interface Identified {
  public String id();
}
