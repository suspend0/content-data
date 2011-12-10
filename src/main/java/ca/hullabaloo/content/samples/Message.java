package ca.hullabaloo.content.samples;

import ca.hullabaloo.content.api.SchemaVersion;
import ca.hullabaloo.content.api.WholeType;

@WholeType("MESSAGE")
@SchemaVersion(1)
public interface Message extends Named {
  public String poster();
}
