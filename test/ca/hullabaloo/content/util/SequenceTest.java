package ca.hullabaloo.content.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

public class SequenceTest {
  @Test(invocationCount = 10)
  public void testSequence() {
    final int count = new Random().nextInt(10000);
    Sequence s = new Sequence(Time.systemTime(), 18, 1);
    long last = 0;
    for (int i = 0; i < count; i++) {
      long current = s.next();
      Assert.assertTrue(last < current);
      last = current;
    }
  }

  @Test
  public void testOrdering() throws InterruptedException {
    Sequence a = new Sequence(Time.systemTime(), 1, 18);
    Sequence b = new Sequence(Time.systemTime(), 2, 28);
    for (int i = 0; i < 10000; i++) {
      Assert.assertTrue(a.next() < b.next());
    }
    Thread.sleep(100);
    for (int i = 0; i < 10000; i++) {
      Assert.assertTrue(a.next() < b.next());
    }
  }
}
