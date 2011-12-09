package ca.hullabaloo.content.impl.finder;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Var;

public class FParser {
  public Node parse(String expression) throws ParseException {
    Grammar parser = Parboiled.createParser(Grammar.class);
    ParsingResult<Node> r = new ReportingParseRunner<Node>(parser.FinderExpression()).run(expression);
    if (r.hasErrors()) {
      throw new ParseException(r.parseErrors);
    }
    return r.resultValue;
  }

  @BuildParseTree
  private static class Grammar extends BaseParser<Node> {
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
              swap() && push(new ExpressionNode(op.get(), pop(), pop()))
          ),
          WhiteSpace()
      );
    }

    public Rule Term() {
      return FirstOf(Atom(), Parens());
    }

    public Rule Atom() {
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
          push(new ValueNode(op.get(), property.get(), value.get()))
      );
    }

    public Rule Parens() {
      return Sequence('(', Expression(), ')');
    }

    @SuppressSubnodes
    public Rule Property() {
      return Sequence(TestNot(Conditional()), Letter(), ZeroOrMore(LetterOrDigit()));
    }

    @SuppressSubnodes
    public Rule Equality() {
      return FirstOf("=", "<", ">");
    }

    @SuppressSubnodes
    public Rule BindVariable() {
      return Sequence(':', OneOrMore(LetterOrDigit()));
    }

    public Rule LetterOrDigit() {
      return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_');
    }

    public Rule Letter() {
      return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_');
    }

    public Object Conditional() {
      return FirstOf(IgnoreCase("and"), IgnoreCase("or"));
    }

    @SuppressNode
    public Rule WhiteSpace() {
      return ZeroOrMore(AnyOf(" \t\f\n\r"));
    }

    @Override
    protected Rule fromCharLiteral(char c) {
      return super.fromCharLiteral(c).suppressNode();
    }
  }

}
