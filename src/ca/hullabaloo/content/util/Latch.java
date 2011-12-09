package ca.hullabaloo.content.util;

import com.google.common.base.Throwables;

public class Latch {
  private volatile boolean locked = true;

  public void await() {
    if (locked) {
      synchronized (this) {
        while (locked) {
          try {
            this.wait();
          } catch (InterruptedException e) {
            throw Throwables.propagate(e);
          }
        }
      }
    }
  }

  public synchronized void release() {
    locked = false;
    this.notifyAll();
  }
}
