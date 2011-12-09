package ca.hullabaloo.content.impl.finder;

import java.util.Set;

public class ValueNode extends Node {
  private final Op comparisonOp;
  private final String property;
  private final String value;

  public ValueNode(String comparisonOp, String property, String value) {
    this.comparisonOp = Op.operator(comparisonOp);
    this.property = property;
    this.value = value;
  }

  public String toString() {
    return String.format("%s%s%s", property, comparisonOp, value);
  }

  @Override
  protected void addParams(Set<String> r) {
    r.add(this.value);
  }

  @Override
  public String mvel() {
    return String.format("%s%s%s", property, comparisonOp.mvel(), MvelExpressions.bindVariableToMvel(value));
  }
}
