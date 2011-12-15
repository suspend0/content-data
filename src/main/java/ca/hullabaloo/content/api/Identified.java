package ca.hullabaloo.content.api;

/**
 * The identity is the {@link ca.hullabaloo.content.api.Stored#key()}
 * plus an incrementing integer.
 */
public interface Identified {
  public String id();
}
