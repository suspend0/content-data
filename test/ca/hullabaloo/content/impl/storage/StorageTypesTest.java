package ca.hullabaloo.content.impl.storage;

import com.google.common.collect.Interner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

public class StorageTypesTest {
  @Test(invocationCount = 5)
  public void testRegister() throws Exception {
    StorageTypes s = new StorageTypes();
    Class[] types = new Class[]{A.class, B_A.class, C_A.class};
    Collections.shuffle(Arrays.asList(types));
    String message = "Order:" + Arrays.toString(types);

    for (Class type : types) {
      s.register(type);
    }

    int id_A = s.id(A.class);
    int id_B = s.id(B_A.class);
    int id_C = s.id(C_A.class);

    Assert.assertFalse(id_A == id_B, message);
    Assert.assertFalse(id_A == id_C, message);
    Assert.assertFalse(id_B == id_C, message);

    int[] expected = {id_A, id_B, id_C};
    int[] actual = s.ids(A.class);
    Arrays.sort(expected);
    Arrays.sort(actual);
    Assert.assertEquals(expected, actual, message);

    Interner<String> props_B = s.properties(B_A.class);
    Assert.assertNotNull(props_B.intern("a"), message);
    Assert.assertNotNull(props_B.intern("b"), message);
    Assert.assertNull(props_B.intern("c"), message);

    Interner<String> props_A = s.properties(A.class);
    Assert.assertNotNull(props_A.intern("a"), message);
    Assert.assertNull(props_A.intern("b"), message);
    Assert.assertNull(props_A.intern("c"), message);
  }

  @SuppressWarnings({"UnusedDeclaration"})
  static interface A {
    String a();
  }

  @SuppressWarnings({"UnusedDeclaration"})
  static interface B_A extends A {
    String b();
  }

  @SuppressWarnings({"UnusedDeclaration"})
  static interface C_A extends A {
    String c();
  }
}
