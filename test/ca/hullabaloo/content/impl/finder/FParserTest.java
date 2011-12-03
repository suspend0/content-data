package ca.hullabaloo.content.impl.finder;

import com.google.common.collect.ImmutableSet;
import org.testng.annotations.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class FParserTest {
  private static Set<String> setOf(String... strings) {
    return ImmutableSet.copyOf(strings);
  }

  @Test
  public void simple() {
    Node n = new FParser().parse("foo=:1");
    assertThat(n.params(), equalTo(setOf(":1")));
    assertThat(n.mvel(), equalTo("foo==_1"));
  }

  @Test
  public void simpleWithParens() {
    Node n = new FParser().parse("(foo=:1)");
    assertThat(n.params(), equalTo(setOf(":1")));
  }

  @Test
  public void and() {
    Node n = new FParser().parse("foo=:foo and bar=:bar");
    assertThat(n.params(), equalTo(setOf(":foo", ":bar")));
  }

  @Test
  public void andOr() {
    Node n = new FParser().parse("foo=:1 and( bar=:2 OR baz = :3 )");
    assertThat(n.params(), equalTo(setOf(":1", ":2", ":3")));
  }
}
