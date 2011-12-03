package ca.hullabaloo.content.impl.finder;

import org.testng.annotations.Test;

public class ParserTest {
  @Test
  public void simple() {
    new Parser().parse("foo=:1");
  }

  @Test
  public void simpleP() {
    new Parser().parse("(foo=:1)");
  }

  @Test
  public void and() {
    new Parser().parse("foo=:foo and bar=:bar");
  }

  @Test
  public void andOr() {
    new Parser().parse("foo=:1 and( bar=:2 OR baz = :3 )");
  }
}
