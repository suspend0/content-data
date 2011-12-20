package ca.hullabaloo.content.api;

import ca.hullabaloo.content.impl.storage.TestStorage;
import ca.hullabaloo.content.samples.Document;
import ca.hullabaloo.content.samples.Message;
import ca.hullabaloo.content.samples.Named;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MultipleTypeTest {
  @Test
  public void query() {
    Storage s = TestStorage.memory();
    s.register(Document.class);
    s.register(Message.class);
    {
      WorkUnit b = s.begin();
      Updater<Document> u = b.updater(Document.class);
      Document $ = u.fields();
      u.forId("DOC-1")
          .set($.name(), "butter")
          .set($.description(), "creamy")
          .set($.body(), "silky");
      u.forId("DOC-3")
          .set($.name(), "bacon")
          .set($.description(), "yummy")
          .set($.body(), "curvy");
      u.forId("DOC-4")
          .set($.name(), "lard")
          .set($.description(), "for baking");
      b.commit();
    }
    {
      WorkUnit b = s.begin();
      Updater<Message> u = b.updater(Message.class);
      Message $ = u.fields();
      u.forId("MESSAGE-5")
          .set($.name(), "hello")
          .set($.poster(), "tony");
      u.forId("MESSAGE-6")
          .set($.name(), "whatever")
          .set($.description(), "creamy")
          .set($.poster(), "foo");
      b.commit();
    }
    {
      Query<Named> q = s.query(Named.class);
      Named $ = q.fields();
      q.withEquals($.name(), "bacon", "whatever");
      IdSet<Named> r = q.execute();

      Assert.assertEquals(r.size(), 2);
      Assert.assertTrue(r.contains(3));
      Assert.assertTrue(r.contains(6));

      q.withEquals($.description(), "creamy");
      r = q.execute();
      Assert.assertEquals(r.size(), 1);
      Assert.assertTrue(r.contains(6));
    }
  }
}
