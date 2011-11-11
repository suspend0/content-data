package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.StorageSpi;
import com.google.common.collect.Interner;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MemoryStorage extends BaseStorage {
  private Queue<byte[]> data = new ConcurrentLinkedQueue<byte[]>();
  private final StorageTypes types = new StorageTypes();
  private final StorageSpi spi = new StorageSpi() {
    @Override
    public Interner<String> properties(Class<?> type) {
      return types.properties(type);
    }

    @Override
    public Iterator<byte[]> data() {
      return data.iterator();
    }

    @Override
    public void append(byte[] bytes) {
      MemoryStorage.this.data.add(bytes);
    }
  };

  @Override
  protected StorageSpi spi() {
    return this.spi;
  }

  @Override
  protected StorageTypes storageTypes() {
    return this.types;
  }
}
