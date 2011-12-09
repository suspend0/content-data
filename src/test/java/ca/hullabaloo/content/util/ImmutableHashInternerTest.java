package ca.hullabaloo.content.util;

import com.google.common.collect.Interner;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ImmutableHashInternerTest {
  @Test
  public void testCreate() throws Exception {
    String a1 = new String("foo".toCharArray());
    String a2 = new String("foo".toCharArray());
    String b1 = new String("bar".toCharArray());
    String b2 = new String("bar".toCharArray());
    String c1 = new String("baz".toCharArray());
    String c2 = new String("baz".toCharArray());

    Assert.assertNotSame(a1, a2);
    Assert.assertNotSame(b1, b2);
    Assert.assertNotSame(c1, c2);

    Interner<String> i = ImmutableHashInterner.create(a1, b1, c1);

    Assert.assertSame(i.intern(a2), a1);
    Assert.assertSame(i.intern(b2), b1);
    Assert.assertSame(i.intern(c2), c1);
    Assert.assertSame(i.intern(c1), c1);
  }
}
