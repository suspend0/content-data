package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Identified;
import ca.hullabaloo.content.api.Stored;
import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

public class StorageTypesTest {
  @Test(invocationCount = 10)
  public void testRegister() throws Exception {
    DefaultStorageTypes s = new DefaultStorageTypes();
    Class[] types = new Class[]{C.class, D.class};
    Collections.shuffle(Arrays.asList(types));
    String message = "Order:" + Arrays.toString(types);

    for (Class type : types) {
      s.register(type);
    }

    ImmutableMap<String, Class> expect_A = ImmutableMap.<String, Class>of("a", A.class);
    ImmutableMap<String, Class> props_A = s.properties(A.class);
    Assert.assertEquals(props_A, expect_A, message);

    ImmutableMap<String, Class> expect_B = ImmutableMap.<String, Class>of("a", A.class, "b", B.class);
    ImmutableMap<String, Class> props_B = s.properties(B.class);
    Assert.assertEquals(props_B, expect_B, message);

    ImmutableMap<String, Class> expect_C = ImmutableMap.<String, Class>of("a", A.class, "c", C.class);
    ImmutableMap<String, Class> props_C = s.properties(C.class);
    Assert.assertEquals(props_C, expect_C, message);

    ImmutableMap<String, Class> expect_D = ImmutableMap.<String, Class>of("a", A.class, "b", B.class, "d", D.class);
    ImmutableMap<String, Class> props_D = s.properties(D.class);
    Assert.assertEquals(props_D, expect_D, message);
  }

  @Stored(schemaVersion = 1)
  static interface A extends Identified {
    String a();
  }

  @Stored(schemaVersion = 1)
  static interface B extends A {
    String b();
  }

  @SuppressWarnings("UnusedDeclaration")
  @Stored(key = "C", schemaVersion = 1)
  static interface C extends A {
    String c();
  }

  @SuppressWarnings("UnusedDeclaration")
  @Stored(key = "D", schemaVersion = 1)
  static interface D extends B {
    String d();
  }

  // ======================================================================
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Method defined on multiple.*")
  public void duplicateMethods() {
    DefaultStorageTypes s = new DefaultStorageTypes();
    s.register(Dup2.class);
  }

  @Stored(schemaVersion = 1)
  interface Dup1 extends Identified {
    @SuppressWarnings("UnusedDeclaration")
    String x();
  }

  @SuppressWarnings("AbstractMethodOverridesAbstractMethod")
  @Stored(key = "DUP", schemaVersion = 1)
  interface Dup2 extends Dup1 {
    @Override
    String x();
  }

  // ======================================================================
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "No extension.*")
  public void noExtension() {
    DefaultStorageTypes s = new DefaultStorageTypes();
    s.register(Ext2.class);
  }

  @SuppressWarnings("UnusedDeclaration")
  @Stored(key = "Ext", schemaVersion = 1)
  interface Ext1 extends Identified {
    String x();
  }

  @SuppressWarnings("UnusedDeclaration")
  @Stored(key = "MoreExt", schemaVersion = 1)
  interface Ext2 extends Ext1 {
    String y();
  }

  // ======================================================================
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Duplicate types for key .*")
  public void noDuplicateKeys() {
    DefaultStorageTypes s = new DefaultStorageTypes();
    s.register(Key1.class);
    s.register(Key2.class);
  }

  @SuppressWarnings("UnusedDeclaration")
  @Stored(key = "Ext", schemaVersion = 1)
  interface Key1 extends Identified {
    String x();
  }

  @SuppressWarnings("UnusedDeclaration")
  @Stored(key = "Ext", schemaVersion = 1)
  interface Key2 extends Identified {
    String y();
  }
}
