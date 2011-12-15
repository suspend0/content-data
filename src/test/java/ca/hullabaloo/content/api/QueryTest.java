package ca.hullabaloo.content.api;

import ca.hullabaloo.content.impl.storage.MemoryStorage;
import ca.hullabaloo.content.samples.Named;
import ca.hullabaloo.content.samples.Thing;
import org.testng.Assert;
import org.testng.annotations.Test;

import static ca.hullabaloo.content.samples.Thing.ID.id;

public class QueryTest {
  @Test
  public void query() {
    Storage s = new MemoryStorage();
    s.register(Thing.class);

    WorkUnit b = s.begin();
    Updater<Named> u = b.updater(Named.class);
    Named $ = u.fields();
    u.forId(id(1))
        .set($.name(), "butter")
        .set($.description(), "creamy");
    u.forId(id(3))
        .set($.name(), "bacon")
        .set($.description(), "yummy");
    u.forId(id(4))
        .set($.name(), "lard")
        .set($.description(), "for baking");
    b.commit();

    Query<Named> q = s.query(Named.class);
    $ = q.fields();
    q.withEquals($.name(), "bacon");
    IdSet<Named> r = q.execute();

    Assert.assertEquals(r.size(), 1);
    Assert.assertTrue(r.contains(3));

    q.withEquals($.description(),"creamy");
    r = q.execute();
    Assert.assertEquals(r.size(), 0);
  }
}
