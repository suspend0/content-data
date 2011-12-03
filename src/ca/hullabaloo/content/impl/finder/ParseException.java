package ca.hullabaloo.content.impl.finder;

import org.parboiled.errors.ErrorUtils;
import org.parboiled.errors.ParseError;

import java.util.List;

public class ParseException extends RuntimeException {
  public ParseException(String expression, List<ParseError> parseErrors) {
    super(ErrorUtils.printParseErrors(parseErrors));
  }
}
