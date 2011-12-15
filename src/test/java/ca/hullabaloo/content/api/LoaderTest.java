package ca.hullabaloo.content.api;

import ca.hullabaloo.content.samples.Named;
import ca.hullabaloo.content.samples.Thing;
import org.testng.annotations.Test;

import java.util.List;

import static ca.hullabaloo.content.samples.Thing.ID.id;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class LoaderTest {
  @Test(dataProvider = "impl", dataProviderClass = StorageTest.class)
  public void simple(Storage s) {
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

    List<Named> r = s.loader(Named.class).getAll(id(1, 2, 3));
    assertThat(r.size(), equalTo(3)); // not 4
    check(r.get(0), id(1), "butter", "creamy");
    assertThat(r.get(1), nullValue());
    check(r.get(2), id(3), "bacon", "yummy");
  }

  private void check(Named named, String id, String name, String description) {
    assertThat(named.id(), equalTo(id));
    assertThat(named.name(), equalTo(name));
    assertThat(named.description(), equalTo(description));
  }
}
