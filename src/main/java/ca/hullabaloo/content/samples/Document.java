package ca.hullabaloo.content.samples;

import ca.hullabaloo.content.api.SchemaVersion;
import ca.hullabaloo.content.api.WholeType;

@WholeType("DOC")
@SchemaVersion(1)
public interface Document extends Named {
  String body();
}
