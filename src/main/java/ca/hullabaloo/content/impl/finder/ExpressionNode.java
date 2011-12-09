package ca.hullabaloo.content.impl.finder;

import java.util.Set;

public class ExpressionNode extends Node {
  private final Op logicalOp;
  private final Node left;
  private final Node right;

  public ExpressionNode(String logicalOp, Node left, Node right) {
    this.logicalOp = Op.operator(logicalOp);
    this.left = left;
    this.right = right;
  }

  public String toString() {
    return String.format("(%s) %s (%s)", left, logicalOp, right);
  }

  public String mvel() {
    return String.format("(%s) %s (%s)", left.mvel(), logicalOp.mvel(), right.mvel());
  }

  @Override
  protected void addParams(Set<String> r) {
    this.left.addParams(r);
    this.right.addParams(r);
  }
}
