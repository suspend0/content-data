package ca.hullabaloo.content.impl.finder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.util.SimpleVariableSpaceModel;

import java.io.Serializable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class FExpression<T> {
  private final Serializable expr;
  private final SimpleVariableSpaceModel model;
  private final int argumentCount;

  FExpression(Node root, Class<T> baseType, String[] paramNames, Class[] paramTypes) {
    this(root, baseType, stitch(paramNames, paramTypes));
  }

  // this must stay private b/c we depend on map iteration order
  private FExpression(Node root, Class<T> baseType, ImmutableMap<String, Class> params) {
    checkArgument(root.params().equals(params.keySet()),
        "mismatched parameters declaration %s, expression %s",
        params.keySet(), root.params());

    String expr = root.mvel();
    params = rename(params);

    ParserContext ctx = new ParserContext();
    ctx.setStrongTyping(true);
    ctx.setIndexAllocation(true);
    ctx.addInput("this", baseType);
    ctx.withIndexedVars(Iterables.toArray(params.keySet(), String.class));
    ctx.addInputs(params);

    // a final validation step
    checkArgument(MVEL.analyze(expr, ctx) == Boolean.class);

    this.model = new SimpleVariableSpaceModel(ctx.getIndexedVarNames());
    this.expr = MVEL.compileExpression(expr, ctx);
    this.argumentCount = params.size();
  }

  private static ImmutableMap<String, Class> rename(ImmutableMap<String, Class> params) {
    ImmutableMap.Builder<String, Class> r = ImmutableMap.builder();
    for (Map.Entry<String, Class> e : params.entrySet()) {
      r.put(FExpressions.bindVariableToMvel(e.getKey()), e.getValue());
    }
    return r.build();
  }

  private static ImmutableMap<String, Class> stitch(String[] paramNames, Class[] paramTypes) {
    checkArgument(paramNames.length == paramTypes.length);
    ImmutableMap.Builder<String, Class> r = ImmutableMap.builder();
    for (int i = 0; i < paramNames.length; i++) {
      r.put(paramNames[i], paramTypes[i]);
    }
    return r.build();
  }

  public boolean evaluate(T item, Object... parameters) {
    checkArgument(parameters.length == argumentCount, "expected %s arguments (got %s)",
        argumentCount, parameters.length);
    Object r = MVEL.executeExpression(this.expr, item, this.model.createFactory(parameters));
    return r == Boolean.TRUE;
  }
}
