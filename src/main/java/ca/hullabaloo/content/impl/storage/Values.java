package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Storage;
import com.google.common.collect.ImmutableList;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

// TODO move stuff around so values can be package private
public class Values {
  @SuppressWarnings({"unchecked"})
  static <T> T make(Class<T> type, final Map<String, Object> values) {
    InvocationHandler fromMap = new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if (ignore.contains(name)) {
          return method.invoke(this);
        }
        return values.get(name);
      }
    };
    return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, fromMap);
  }

  @SuppressWarnings({"unchecked"})
  public static <T> T proxy(final Class<T> type, final MethodCallback cb) {
    return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        checkArgument(!Storage.ID_METHOD_NAME.equals(name));
        if (name.equals("toString")) {
          return "Proxy{" + type.getName() + "}";
        }
        if (name.equals("hashCode")) {
          return System.identityHashCode(this);
        }
        if (name.equals("equals") && args.length == 1) {
          return this == args[0];
        }
        cb.called(name);
        return null;
      }
    });
  }

  public interface MethodCallback {
    void called(String methodName);
  }

  private static final ImmutableList<String> ignore = ImmutableList.of("toString", "hashCode");
}
