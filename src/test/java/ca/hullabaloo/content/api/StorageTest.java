package ca.hullabaloo.content.api;

import ca.hullabaloo.content.impl.storage.HawtStorage;
import ca.hullabaloo.content.impl.storage.MemoryStorage;
import ca.hullabaloo.content.util.Guava;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StorageTest {
  private static Queue<File> temps = new ConcurrentLinkedQueue<File>();

  @DataProvider(name = "impl")
  public static Object[][] instances() {
    File dir = Files.createTempDir();
    temps.add(dir);
    return new Object[][]{
        {new MemoryStorage()},
        {new HawtStorage(new EventBus(), dir)}
    };
  }

  @AfterSuite
  public static void cleanup() {
    File dir;
    while (null != (dir = temps.poll())) {
      try {
        Guava.deleteRecursively(dir);
      } catch (IOException e) {
        e.printStackTrace(); // but keep going
      }
    }
  }
}
