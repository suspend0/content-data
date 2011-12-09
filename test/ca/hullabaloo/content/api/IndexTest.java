package ca.hullabaloo.content.api;

import ca.hullabaloo.content.impl.storage.MemoryStorage;
import ca.hullabaloo.content.samples.Named;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IndexTest {
  @Test
  public void query() {
    // we update & run the query again, but we expect the index layer to catch it
    Storage s = new MemoryStorage().maxReads(1);
    s.register(Named.class);
    {
      WorkUnit b = s.begin();
      Updater<Named> u = b.updater(Named.class);
      Named $ = u.fields();
      u.forId(1)
          .set($.name(), "butter")
          .set($.description(), "creamy");
      u.forId(3)
          .set($.name(), "bacon")
          .set($.description(), "yummy");
      u.forId(4)
          .set($.name(), "lard")
          .set($.description(), "for baking");
      b.commit();
    }

    Query<Named> q = s.query(Named.class);
    {
      Named $ = q.fields();
      q.withEquals($.name(), "bacon");
      IdSet<Named> r = q.execute();

      Assert.assertEquals(r.size(), 1);
      Assert.assertTrue(r.contains(3));
    }

    {
      WorkUnit b = s.begin();
      Updater<Named> u = b.updater(Named.class);
      Named $ = u.fields();
      u.forId(5)
          .set($.name(), "bacon")
          .set($.description(), "peppery");
      b.commit();
      IdSet<Named> r = q.execute();
      Assert.assertEquals(r.size(), 2);
      Assert.assertTrue(r.contains(3));
      Assert.assertTrue(r.contains(5));
    }
  }
}
