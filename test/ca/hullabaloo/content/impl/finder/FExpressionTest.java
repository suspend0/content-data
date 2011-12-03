package ca.hullabaloo.content.impl.finder;

import org.testng.annotations.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FExpressionTest {
  private final Bean a = new Bean("a", 1, null);
  private final Bean b = new Bean("b", 2, null);

  @Test
  public void evaluate() {
    FExpression<Bean> ex = new FExpression<Bean>(
        new FParser().parse("name=:1"), Bean.class,
        new String[]{":1"},
        new Class[]{String.class}
    );
    assertThat(ex.evaluate(a, "a"), is(true));
    assertThat(ex.evaluate(b, "a"), is(false));
    assertThat(ex.evaluate(a, "b"), is(false));
    assertThat(ex.evaluate(b, "b"), is(true));
  }

  @Test
  public void twoArguments() {
    FExpression<Bean> ex = new FExpression<Bean>(
        new FParser().parse("name=:1 and value = :val"), Bean.class,
        new String[]{":1", ":val"},
        new Class[]{String.class, String.class}
    );
    assertThat(ex.evaluate(a, "a", 1), is(true));
    assertThat(ex.evaluate(b, "b", 1), is(false));
    assertThat(ex.evaluate(a, "a", 2), is(false));
    assertThat(ex.evaluate(b, "b", 2), is(true));
  }

  @SuppressWarnings({"RedundantStringConstructorCall"})
  public static class Bean {
    public final String name;
    public final long value;
    public final Map<String, Integer> counts;

    public Bean(String name, long value, Map<String, Integer> counts) {
      this.name = name.substring(0);
      this.value = value;
      this.counts = counts;
    }
  }
}
