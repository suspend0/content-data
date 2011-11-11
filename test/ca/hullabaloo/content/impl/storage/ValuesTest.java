package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.samples.Named;
import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ValuesTest {
  @Test
  public void testMake() throws Exception {
    Named named = Values.make(Named.class, ImmutableMap.<String, Object>of("name", "bob", "description", "cool"));
    Assert.assertEquals(named.name(), "bob");
    Assert.assertEquals(named.description(), "cool");
  }
}
