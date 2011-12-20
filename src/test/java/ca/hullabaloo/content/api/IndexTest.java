package ca.hullabaloo.content.api;

import ca.hullabaloo.content.impl.storage.TestStorage;
import ca.hullabaloo.content.samples.Named;
import ca.hullabaloo.content.samples.Thing;
import org.testng.Assert;
import org.testng.annotations.Test;

import static ca.hullabaloo.content.samples.Thing.ID.id;

public class IndexTest {
  @Test
  public void query() {
    // we update & run the query again, but we expect the index layer to catch it
    Storage s = TestStorage.memoryWithMaxReads(1);
    s.register(Thing.class);
    {
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
      u.forId(id(5))
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
