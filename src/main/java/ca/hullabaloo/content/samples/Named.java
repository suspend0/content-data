package ca.hullabaloo.content.samples;

import ca.hullabaloo.content.api.Identified;
import ca.hullabaloo.content.api.Stored;

@Stored(schemaVersion = 1)
public interface Named extends Identified {
  public String id();

  public String name();

  public String description();
}
