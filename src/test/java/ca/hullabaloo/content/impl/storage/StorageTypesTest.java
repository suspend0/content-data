package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.WholeType;
import ca.hullabaloo.content.api.SchemaVersion;
import ca.hullabaloo.content.util.InternSet;
import com.google.common.collect.ImmutableSet;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class StorageTypesTest {
  @Test(invocationCount = 1)
  public void testRegister() throws Exception {
    StorageTypes s = new StorageTypes();
    Class[] types = new Class[]{A.class, B.class, C.class};
    Collections.shuffle(Arrays.asList(types));
    String message = "Order:" + Arrays.toString(types);

    for (Class type : types) {
      s.register(type);
    }

    InternSet<String> props_A = s.properties(A.class);
    Assert.assertNotNull(props_A.intern("a"), message);
    Assert.assertNull(props_A.intern("b"), message);
    Assert.assertNull(props_A.intern("c"), message);
    Assert.assertNull(props_A.intern("id"), message);
    Assert.assertEquals(s(A.class), s(s.componentsOf(A.class)));

    InternSet<String> props_B = s.properties(B.class);
    Assert.assertNull(props_B.intern("a"), message);
    Assert.assertNotNull(props_B.intern("b"), message);
    Assert.assertNull(props_B.intern("c"), message);
    Assert.assertNull(props_B.intern("id"), message);
    Assert.assertEquals(s(A.class,B.class), s(s.componentsOf(B.class)));

    InternSet<String> props_C = s.properties(C.class);
    Assert.assertNull(props_C.intern("a"), message);
    Assert.assertNull(props_C.intern("b"), message);
    Assert.assertNotNull(props_C.intern("c"), message);
    Assert.assertNull(props_C.intern("id"), message);
    Assert.assertEquals(s(A.class,C.class), s(s.componentsOf(C.class)));
  }

  private static Set<Class<?>> s(Class<?>... classes) {
    return ImmutableSet.copyOf(classes);
  }

  private static Set<Class<?>> s(InternSet<Class<?>> classes) {
    return ImmutableSet.copyOf(classes.iterator());
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @WholeType("A")
  @SchemaVersion(1)
  static interface A extends AX {
    String a();
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @WholeType("B")
  @SchemaVersion(1)
  static interface B extends A {
    String b();
  }

  static interface AX {
    String b();
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @WholeType("C")
  @SchemaVersion(1)
  static interface C extends A {
    String c();
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @SchemaVersion(1)
  static interface D extends B {
    String d();
  }
}
