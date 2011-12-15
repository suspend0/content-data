package ca.hullabaloo.content.samples;

import ca.hullabaloo.content.api.Stored;
import ca.hullabaloo.content.impl.StoredAnnotation;

@Stored(key = "THING", schemaVersion = 1)
public interface Thing extends Named {
  public static class ID {
    public static String[] id(int first, int second, int... rest) {
      String base = StoredAnnotation.getKey(Thing.class) + "-";
      String[] result = new String[rest.length + 2];
      result[0] = base + first;
      result[1] = base + second;
      for (int i = 0; i < rest.length; i++) {
        result[i + 2] = base + rest[i];
      }
      return result;
    }

    public static String id(int id) {
      return StoredAnnotation.getKey(Thing.class) + "-" + id;
    }
  }
}
