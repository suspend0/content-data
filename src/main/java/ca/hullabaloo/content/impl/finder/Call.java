package ca.hullabaloo.content.impl.finder;

import ca.hullabaloo.content.api.IdIterator;
import ca.hullabaloo.content.api.IdSet;
import ca.hullabaloo.content.api.Storage;
import ca.hullabaloo.content.impl.StoredAnnotation;
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
    String base = StoredAnnotation.getKey(resultType) + "-";
    IdSet<?> x = storage.get().query(resultType).execute();
    String[] ids = new String[x.size()];
    int pos = 0;
    for (IdIterator it = x.iterator(); it.hasNext(); ) {
      ids[pos++] = base + it.next();
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
