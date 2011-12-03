package ca.hullabaloo.content.impl.finder;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.buffers.DefaultInputBuffer;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Var;

public class Parser {
  public void parse(String expression) {
    System.out.println("= = = = = = = = = = = = = = = =");
    System.out.println("input '" + expression + "'");
    InputBuffer input = new DefaultInputBuffer(expression.toCharArray());
    Grammar parser = Parboiled.createParser(Grammar.class);
    ParsingResult<FNode> r = new ReportingParseRunner<FNode>(parser.FinderExpression()).run(input);
    if (r.hasErrors()) {
      System.out.println(ErrorUtils.printParseErrors(r.parseErrors));
      return;
    }
    FNode n = r.resultValue;
    System.out.println(n);
//    String t = GraphUtils.printTree(r.parseTreeRoot, new ToStringFormatter<GraphNode>(null));
//    System.out.println(t);
//    Node<?> q = ParseTreeUtils.findNodeByLabel(r.parseTreeRoot, "Expression");
//    System.out.printf("expression=>%s (%s)\n", q, q.getChildren().size());

//    Node<?> s = r.parseTreeRoot.getChildren().get(0);
//    System.out.println("s=>" + s);
//    for (Node<?> node : q.getChildren()) {
//      System.out.printf("%s:'%s'\n", node.getLabel(), ParseTreeUtils.getNodeText(node, input));
//    }
  }

  @BuildParseTree
  private static class Grammar extends BaseParser<FNode> {
    public Grammar() {/**required for parboiled*/}

    public Rule FinderExpression() {
      return Sequence(Expression(), EOI);
    }

    public Rule Expression() {
      Var<String> op = new Var<String>(); // we use an action variable to hold the operator character
      return Sequence(
          WhiteSpace(),
          Term(),
          ZeroOrMore(
              Conditional(), op.set(match()),
              Term(),

              // create an AST node for the operation that was just matched
              // we consume the two top stack elements and replace them with a new AST node
              // we use an alternative technique to the one shown in CalculatorParser1 to reverse
              // the order of the two top value stack elements
              swap() && push(new ExNode(op.get(), pop(), pop()))
          ),
          WhiteSpace()
      );
    }

    Rule Term() {
      return FirstOf(Atom(), Parens());
    }

    Rule Atom() {
      Var<String> property = new Var<String>();
      Var<String> op = new Var<String>();
      Var<String> value = new Var<String>();
      return Sequence(
          WhiteSpace(),
          Property(), property.set(match()),
          WhiteSpace(),
          Equality(), op.set(match()),
          WhiteSpace(),
          BindVariable(), value.set(match()),
          WhiteSpace(),

          // same as in CalculatorParser2
          push(new ValNode(op.get(), property.get(), value.get()))
      );
    }

    Rule Parens() {
      return Sequence('(', Expression(), ')');
    }

    @SuppressSubnodes
    Rule Property() {
      return Sequence(TestNot(Conditional()), Letter(), ZeroOrMore(LetterOrDigit()));
    }

    @SuppressSubnodes
    Rule Equality() {
      return FirstOf("=", "<", ">");
    }

    @SuppressSubnodes
    Rule BindVariable() {
      return Sequence(':', OneOrMore(LetterOrDigit()));
    }

    Rule LetterOrDigit() {
      return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_');
    }

    Rule Letter() {
      return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_');
    }

    Object Conditional() {
      return FirstOf(IgnoreCase("and"), IgnoreCase("or"));
    }

    @SuppressNode
    Rule WhiteSpace() {
      return ZeroOrMore(AnyOf(" \t\f\n\r"));
    }

    @Override
    protected Rule fromCharLiteral(char c) {
      return super.fromCharLiteral(c).suppressNode();
    }
  }

  public static class FNode {
  }

  public static class ValNode extends FNode {
    private final String operator;
    private final String property;
    private final String value;

    public ValNode(String operator, String property, String value) {
      this.operator = operator;
      this.property = property;
      this.value = value;
    }

    public String toString() {
      return "Val{" + property + operator + value + "}";
    }
  }

  public static class ExNode extends FNode {

    private final String conditional;
    private final FNode left;
    private final FNode right;

    public ExNode(String conditional, FNode left, FNode right) {
      this.conditional = conditional;
      this.left = left;
      this.right = right;
    }

    public String toString() {
      return "Ex{" + left + " " + conditional + " " + right + "}";
    }
  }
}
