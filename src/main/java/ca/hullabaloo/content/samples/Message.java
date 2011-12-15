package ca.hullabaloo.content.samples;

import ca.hullabaloo.content.api.Stored;

@Stored(key = "MESSAGE", schemaVersion = 1)
public interface Message extends Named {
  public String poster();
}
