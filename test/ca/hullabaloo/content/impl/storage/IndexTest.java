package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Update;
import ca.hullabaloo.content.impl.ArrayIdSet;
import com.google.common.base.Predicates;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

public class IndexTest {
  private UpdateBatch Update(Class<?> type, int id, String field, String value) {
    return new UpdateBatch(Collections.singletonList(new Update(type, id, field, value)));
  }

  @Test
  public void testAdd() {
    Index<String> idx = new Index<String>(String.class, "in", Predicates.containsPattern("ok"));
    idx.update(Update(String.class, 1, "in", "ok"));
    idx.update(Update(Integer.class, 2, "in", "ok"));
    idx.update(Update(String.class, 3, "x", "ok"));
    idx.update(Update(String.class, 3, "in", "x"));

    Assert.assertEquals(idx.get(), ArrayIdSet.ofSorted(1));
  }

  @Test
  public void testRemove() {
    Index<String> idx = new Index<String>(String.class, "in", Predicates.containsPattern("ok"));
    idx.update(Update(String.class, 1, "in", "ok"));
    idx.update(Update(String.class, 2, "in", "ok"));
    idx.update(Update(String.class, 1, "in", "nope"));
    idx.update(Update(Integer.class, 2, "in", "nope"));//wrong type

    Assert.assertEquals(idx.get(), ArrayIdSet.ofSorted(2));
  }
}
