package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Storage;

import java.io.File;

public class TestStorage {
  public static Storage hawt(File dir) {
    DefaultStorageTypes types = new DefaultStorageTypes();
    return new BaseStorage(types, new HawtLogStorage(dir), new MemoryObjectStorage(types));
  }

  public static Storage memory() {
    return memoryWithMaxReads(Integer.MAX_VALUE);
  }

  public static Storage memoryWithMaxReads(int maxReads) {
    MemoryLogStorage log = new MemoryLogStorage();
    log.maxReads(maxReads);
    DefaultStorageTypes types = new DefaultStorageTypes();
    MemoryObjectStorage objects = new MemoryObjectStorage(types);
    return new BaseStorage(types, log, objects);
  }
}
