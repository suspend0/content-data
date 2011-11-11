package ca.hullabaloo.content.util;

public class Latch {
  private volatile boolean locked = true;

  public void await() {
    if (locked) {
      synchronized (this) {
        while (locked) {
          try {
            this.wait();
          } catch (InterruptedException e) {
            // swallow;
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
