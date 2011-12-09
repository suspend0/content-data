package ca.hullabaloo.content.impl.finder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

class DynamicFinder implements InvocationHandler {
  private final Map<Method, Call> methods;

  public DynamicFinder(Map<Method, Call> methods) {
    this.methods = methods;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Call call = this.methods.get(method);
    if (call != null) {
      return call.invoke(args);
    }
    if (method.getDeclaringClass() == Object.class) {
      if (method.getName().equals("toString")) {
        return "DynamicFinder@" + Integer.toHexString(System.identityHashCode(proxy));
      }
      if (method.getName().equals("hashCode")) {
        return System.identityHashCode(proxy);
      }
      if (method.getName().equals("equals")) {
        return proxy == args[0];
      }

      return method.invoke(this, args);
    }

    throw new Error(method.toString());
  }
}
