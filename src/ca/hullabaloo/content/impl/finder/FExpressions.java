package ca.hullabaloo.content.impl.finder;

public class FExpressions {
  public static String bindVariableToMvel(String bindVariable) {
    assert bindVariable.charAt(0) == ':';
    return "_" + bindVariable.substring(1);
  }

  public static String conditionalToMvel(String conditional) {
    if ("and".equals(conditional)) {
      return "&&";
    }
    if ("or".equals(conditional)) {
      return "||";
    }
    throw new IllegalArgumentException("unknown: " + conditional);
  }
}
