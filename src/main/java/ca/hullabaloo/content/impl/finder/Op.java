package ca.hullabaloo.content.impl.finder;

enum Op {
  @SuppressWarnings({"UnusedDeclaration"})
  EQUALS("=", "=="),
  @SuppressWarnings({"UnusedDeclaration"})
  AND("AND", "&&"),
  @SuppressWarnings({"UnusedDeclaration"})
  OR("OR", "||");

  public static Op operator(String token) {
    for (Op op : values()) {
      if (op.expr.equalsIgnoreCase(token)) {
        return op;
      }
    }
    for (Op op : values()) {
      if (op.mvel.equalsIgnoreCase(token)) {
        return op;
      }
    }
    throw new IllegalArgumentException(token);
  }

  private final String expr;
  private final String mvel;

  Op(String expr, String mvel) {
    this.expr = expr;
    this.mvel = mvel;
  }

  public String toString() {
    return expr;
  }

  public String mvel() {
    return this.mvel;
  }
}
