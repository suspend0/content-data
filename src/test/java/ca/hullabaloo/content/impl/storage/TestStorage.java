package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Storage;

import java.io.File;

public class TestStorage {
  public static Storage hawt(File dir) {
    return new BaseStorage(new HawtLogStorage(dir));
  }

  public static Storage memory() {
    return memoryWithMaxReads(Integer.MAX_VALUE);
  }

  public static Storage memoryWithMaxReads(int maxReads) {
    MemoryLogStorage spi = new MemoryLogStorage();
    spi.maxReads(maxReads);
    return new BaseStorage(spi);
  }
}
