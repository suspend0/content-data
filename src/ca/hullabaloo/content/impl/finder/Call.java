package ca.hullabaloo.content.impl.finder;

import ca.hullabaloo.content.api.IdIterator;
import ca.hullabaloo.content.api.IdSet;
import ca.hullabaloo.content.api.Storage;
import com.google.common.collect.Lists;
import com.google.inject.Provider;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

class Call {
  private final MvelExpression<Object> expression;
  private final Provider<Storage> storage;
  private final Class<?> resultType;

  public Call(Method finderMethod, Provider<Storage> storage) {
    this.expression = DynamicFinders.createExpression(finderMethod);
    this.resultType = DynamicFinders.resultType(finderMethod);
    this.storage = storage;
  }

  public Object invoke(Object[] args) {
    IdSet<?> x = storage.get().query(resultType).execute();
    int[] ids = new int[x.size()];
    int pos = 0;
    for (IdIterator it = x.iterator(); it.hasNext(); ) {
      ids[pos++] = it.next();
    }
    List<?> y = storage.get().loader(resultType).getAll(ids);
    y = Lists.newArrayList(y);
    for (Iterator<?> it = y.iterator(); it.hasNext(); ) {
      if (!expression.evaluate(it.next(), args)) {
        it.remove();
      }
    }
    return y.iterator();
  }
}
