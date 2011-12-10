package ca.hullabaloo.content.impl.finder;

import ca.hullabaloo.content.api.Finder;
import ca.hullabaloo.content.api.SchemaVersion;
import ca.hullabaloo.content.api.WholeType;
import com.google.inject.name.Named;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MvelExpressionTest {
  private final Bean a = new BeanImpl("a", 1, null);
  private final Bean b = new BeanImpl("b", 2, null);

  public static interface Coffee {
    @Finder("name=:n")
    public Iterator<Bean> some(@Named(":n") String name);
    @Finder("name=:1")
    public Iterator<Bean> more(String name);
  }

  @Test
  public void fromMethodNamed() throws NoSuchMethodException {
    MvelExpression<Bean> ex = DynamicFinders.createExpression(Coffee.class.getMethod("some", String.class));
    assertThat(ex.evaluate(a, "a"), is(true));
    assertThat(ex.evaluate(b, "a"), is(false));
    assertThat(ex.evaluate(a, "b"), is(false));
    assertThat(ex.evaluate(b, "b"), is(true));
  }
  @Test
  public void fromMethodIndexed() throws NoSuchMethodException {
    MvelExpression<Bean> ex = DynamicFinders.createExpression(Coffee.class.getMethod("more", String.class));
    assertThat(ex.evaluate(a, "a"), is(true));
    assertThat(ex.evaluate(b, "a"), is(false));
    assertThat(ex.evaluate(a, "b"), is(false));
    assertThat(ex.evaluate(b, "b"), is(true));
  }

  @Test
  public void simple() {
    MvelExpression<Bean> ex = new MvelExpression<Bean>(
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
    MvelExpression<Bean> ex = new MvelExpression<Bean>(
        new FParser().parse("name=:1 and value = :val"), Bean.class,
        new String[]{":1", ":val"},
        new Class[]{String.class, String.class}
    );
    assertThat(ex.evaluate(a, "a", 1), is(true));
    assertThat(ex.evaluate(b, "b", 1), is(false));
    assertThat(ex.evaluate(a, "a", 2), is(false));
    assertThat(ex.evaluate(b, "b", 2), is(true));
  }

  @WholeType("BEAN")
  @SchemaVersion(1)
  public interface Bean {
    public String name();
    public String value();
  }

  @SuppressWarnings({"RedundantStringConstructorCall"})
  public static class BeanImpl implements Bean {
    private final String name;
    private final long value;
    private final Map<String, Integer> counts;

    public BeanImpl(String name, long value, Map<String, Integer> counts) {
      this.name = name.substring(0);
      this.value = value;
      this.counts = counts;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public String value() {
      return String.valueOf(value);
    }
  }
}
