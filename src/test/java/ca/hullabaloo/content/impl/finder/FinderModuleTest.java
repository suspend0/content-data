package ca.hullabaloo.content.impl.finder;

import ca.hullabaloo.content.api.Finder;
import ca.hullabaloo.content.api.Finders;
import ca.hullabaloo.content.api.Storage;
import ca.hullabaloo.content.api.Update;
import ca.hullabaloo.content.impl.storage.TestStorage;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FinderModuleTest {
  static class M extends AbstractModule {
    @Override
    protected void configure() {
      DynamicFinders.add(binder(), MyFinder.class);
    }
    
    @Singleton
    @Provides
    public Storage provideStorage() {
      return TestStorage.memory();
    }
  }

  @Test
  public void interception() {
    // TODO: why do we need storage in this test?  Maybe simpler Iterable<Bean> as adapter?
    Injector injector = Guice.createInjector(new M(), new FinderModule());
    Storage storage = injector.getInstance(Storage.class);
    Class<MvelExpressionTest.Bean> t = MvelExpressionTest.Bean.class;
    storage.register(t);
    storage.begin()
        .add(new Update("BEAN-1", "name", "bob"))
        .add(new Update("BEAN-1", "value", "27"))
        .add(new Update("BEAN-2", "name", "lou"))
        .add(new Update("BEAN-2", "value", "23"))
        .commit();

    MyFinder a = injector.getInstance(MyFinder.class);
    List<MvelExpressionTest.Bean> r = Lists.newArrayList(a.foo("27"));
    assertThat(Iterables.getOnlyElement(r).name(), equalTo("bob"));
    assertThat(Iterables.getOnlyElement(r).value(), equalTo("27"));
  }

  @Test
  public void basics() {
    Injector injector = Guice.createInjector(new M(), new FinderModule());
    MyFinder a = injector.getInstance(MyFinder.class);
    MyFinder a1 = injector.getInstance(MyFinder.class);
    Assert.assertSame(a, a1);
    MyFinder b = Guice.createInjector(new M(), new FinderModule()).getInstance(MyFinder.class);
    Assert.assertNotEquals(a, b);
    Assert.assertNotEquals(a.hashCode(), b.hashCode());
    Assert.assertEquals(a, a);
    Assert.assertEquals(a.hashCode(), a.hashCode());
    Assert.assertTrue(a.toString().startsWith("DynamicFinder@"));
  }

  public static interface MyFinder extends Finders {
    @Finder("value=:1")
    public Iterator<MvelExpressionTest.Bean> foo(String val);
  }
}
