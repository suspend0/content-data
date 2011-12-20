package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.LogStorageSpi;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryStorage extends BaseStorage {
  private static class MemoryStorageSpi implements LogStorageSpi {
    private volatile int maxReads = Integer.MAX_VALUE;
    private final AtomicInteger reads = new AtomicInteger(0);
    private final Queue<byte[]> data = new ConcurrentLinkedQueue<byte[]>();

    @Override
    public Iterator<byte[]> data() {
      Preconditions.checkState(reads.getAndIncrement() < maxReads, "only allowed to read %s times", maxReads);
      return data.iterator();
    }

    @Subscribe
    public void updates(UpdateBatch updates) {
      this.data.add(updates.bytes());
    }
  }

  private static ThreadLocal<MemoryStorageSpi> hack = new ThreadLocal<MemoryStorageSpi>();
  private static MemoryStorageSpi superHack(boolean c) {
    if(c) {
      hack.set(new MemoryStorageSpi());
      return hack.get();
    } else {
      MemoryStorageSpi r = hack.get();
      hack.remove();
      return r;
    }
  }

  private final MemoryStorageSpi spi;

  public MemoryStorage() {
    super(superHack(true));
    this.spi = superHack(false);
  }

  public MemoryStorage maxReads(int max) {
    this.spi.maxReads = max;
    return this;
  }
}
