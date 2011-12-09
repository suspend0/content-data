package ca.hullabaloo.content.impl.finder;

public class MvelExpressions {
  public static String bindVariableToMvel(String bindVariable) {
    assert bindVariable.charAt(0) == ':';
    return "_" + bindVariable.substring(1);
  }

}
