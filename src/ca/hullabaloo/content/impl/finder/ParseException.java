package ca.hullabaloo.content.impl.finder;

import org.parboiled.errors.ErrorUtils;
import org.parboiled.errors.ParseError;

import java.util.List;

public class ParseException extends RuntimeException {
  public ParseException(List<ParseError> parseErrors) {
    super(ErrorUtils.printParseErrors(parseErrors));
  }

  public ParseException(String message) {
    super(message);
  }

  public ParseException(Throwable cause) {
    super(cause);
  }
}
