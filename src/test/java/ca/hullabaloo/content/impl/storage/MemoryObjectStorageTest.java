package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Identified;
import ca.hullabaloo.content.api.ObjectStorageSpi;
import ca.hullabaloo.content.api.Stored;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class MemoryObjectStorageTest {
  EventBus bus = new EventBus();
  DefaultStorageTypes types = new DefaultStorageTypes();
  ObjectStorageSpi instance = new MemoryObjectStorage(types);

  @BeforeClass
  public void setUp() {
    types.register(A.class);
    bus.register(instance);
  }

  @Test
  public void storedType() {
    bus.post(new UpdateBatch(ImmutableList.of(new UpdateRecord(A.class, 1, A.class, "name", "foo"))));
    List<A> items = instance.get(A.class, ImmutableList.of("A-1"));
    A item = Iterables.getOnlyElement(items);
    Assert.assertEquals(item.name(), "foo");
    Assert.assertEquals(item.id(), "A-1");
  }

  @Test(dependsOnMethods = "storedType")
  public void queryType() {
    List<N> items = instance.get(N.class, ImmutableList.of("A-1"));
    N item = Iterables.getOnlyElement(items);
    Assert.assertEquals(item.name(), "foo");
    Assert.assertEquals(item.id(), "A-1");
  }

  @Stored(key = "A", schemaVersion = 1)
  public static interface A extends Identified {
    public String name();
  }

  public static interface N extends Identified {
    public String name();
  }
}
