package ca.hullabaloo.content.samples;

import ca.hullabaloo.content.api.SchemaVersion;
import ca.hullabaloo.content.api.WholeType;

@WholeType("N")
@SchemaVersion(1)
public interface Named {
  public int id();
  public String name();
  public String description();
}
