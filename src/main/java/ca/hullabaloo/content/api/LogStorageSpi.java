package ca.hullabaloo.content.api;

import java.util.Iterator;

public interface LogStorageSpi {
  Iterator<byte[]> data(); //TODO add since-sequence
}
