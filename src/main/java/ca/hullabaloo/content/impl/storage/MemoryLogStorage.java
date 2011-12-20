package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.LogStorageSpi;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

class MemoryLogStorage implements LogStorageSpi {
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

  public MemoryLogStorage maxReads(int max) {
    this.maxReads = max;
    return this;
  }
}
