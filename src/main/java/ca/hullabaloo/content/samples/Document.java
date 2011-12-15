package ca.hullabaloo.content.samples;

import ca.hullabaloo.content.api.Stored;

@Stored(key = "DOC", schemaVersion = 1)
public interface Document extends Named {
  String body();
}
