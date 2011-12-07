package ca.hullabaloo.content.impl.finder;

import ca.hullabaloo.content.api.Finder;
import ca.hullabaloo.content.api.Finders;
import ca.hullabaloo.content.api.Storage;
import ca.hullabaloo.content.api.Update;
import ca.hullabaloo.content.impl.storage.MemoryStorage;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
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
      bind(Storage.class).to(MemoryStorage.class).in(Scopes.SINGLETON);
    }
  }

  @Test
  public void interception() {
    Injector injector = Guice.createInjector(new M(), new FinderModule());
    Storage storage = injector.getInstance(Storage.class);
    Class<MvelExpressionTest.Bean> t = MvelExpressionTest.Bean.class;
    storage.register(t);
    storage.begin()
        .add(new Update(t, 1, "name", "bob"))
        .add(new Update(t, 1, "value", "17"))
        .commit();

    MyFinder a = injector.getInstance(MyFinder.class);
    List<MvelExpressionTest.Bean> r = Lists.newArrayList(a.foo("17"));
    assertThat(Iterables.getOnlyElement(r).name(), equalTo("bob"));
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
