package ca.hullabaloo.content.api;

import ca.hullabaloo.content.samples.Named;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class LoaderTest {
  @Test(dataProvider = "impl", dataProviderClass = StorageTest.class)
  public void simple(Storage s) {
    s.register(Named.class);

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

    List<Named> r = s.loader(Named.class).getAll(1, 2, 3);
    assertThat(r.size(), equalTo(3)); // not 4
    check(r.get(0), 1, "butter", "creamy");
    assertThat(r.get(1), nullValue());
    check(r.get(2), 3, "bacon", "yummy");
  }

  private void check(Named named, int id, String name, String description) {
    assertThat(named.id(), equalTo(id));
    assertThat(named.name(), equalTo(name));
    assertThat(named.description(), equalTo(description));
  }
}
