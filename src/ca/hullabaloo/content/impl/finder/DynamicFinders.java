package ca.hullabaloo.content.impl.finder;

import ca.hullabaloo.content.api.Finder;
import ca.hullabaloo.content.api.Finders;
import ca.hullabaloo.content.api.Storage;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.name.Named;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

class DynamicFinders {

  public static void add(Binder binder, Class<?> first, Class<?>... rest) {
    binder = binder.skipSources(DynamicFinders.class);
    for (Class<?> type : Lists.asList(first, rest)) {
      checkTypeAndAnnotations(binder, type);
      bind(binder, type);
    }
  }

  private static void checkTypeAndAnnotations(Binder binder, Class<?> type) {
    if (!type.isInterface()) {
      binder.addError("Type %s must be an interface", type);
    }
    if (!Finders.class.isAssignableFrom(type)) {
      binder.addError("Type %s must be annotated with %s", type, Finders.class);
    }
    for (Method method : type.getMethods()) {
      if (!method.isAnnotationPresent(Finder.class)) {
        binder.addError("Method %s must be annotated with %s", method, Finder.class);
      }
      if (method.getReturnType() != Iterator.class) {
        binder.addError("Method %s must return an %s", method, Iterator.class);
      } else if (!resultType(method).isInterface()) {
        binder.addError("%s must be an interface on method %s", resultType(method), method);
      }
    }
  }

  private static <T> void bind(Binder binder, Class<T> type) {
    ImmutableMap.Builder<Method, Call> methods = ImmutableMap.builder();
    for (Method method : type.getMethods()) {
      try {
        Call call = new Call(method, binder.getProvider(Storage.class));
        methods.put(method, call);
      } catch (ParseException e) {
        binder.addError("%s\n%s", method, e.getMessage());
      }
    }
    binder.bind(type).toInstance(proxy(type, methods.build()));
  }

  @SuppressWarnings({"unchecked"})
  private static <T> T proxy(Class<T> type, Map<Method, Call> methods) {
    return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new DynamicFinder(methods));
  }

  static Class<?> resultType(Method finderMethod) {
    checkArgument(finderMethod.getReturnType() == Iterator.class);
    Type t = finderMethod.getGenericReturnType();
    ParameterizedType pt = (ParameterizedType) t;
    Type r = pt.getActualTypeArguments()[0];
    return (Class<?>) r;
  }

  static <T> MvelExpression<T> createExpression(Method finderMethod) {
    Finder finder;
    checkArgument(null != (finder = finderMethod.getAnnotation(Finder.class)));
    checkArgument(finderMethod.getReturnType() == Iterator.class);

    // collect the return type, argument types and names from the method declaration
    ParameterizedType genericReturnType = (ParameterizedType)
        finderMethod.getGenericReturnType();
    @SuppressWarnings({"unchecked"})
    Class<T> resultType = (Class<T>) genericReturnType.getActualTypeArguments()[0];
    List<String> argumentNames = Lists.newArrayList();
    List<Class<?>> argumentTypes = Lists.newArrayList(finderMethod.getParameterTypes());
    Annotation[][] annotations = finderMethod.getParameterAnnotations();
    for (int i = 0; i < annotations.length; i++) {
      String name = ":" + (1 + i);
      for (int j = 0; j < annotations[i].length; j++) {
        if (annotations[i][j] instanceof Named) {
          Named named = (Named) annotations[i][j];
          name = named.value();
        }
      }
      argumentNames.add(name);
    }
    return createExpression(finder, resultType, argumentNames, argumentTypes);
  }

  static <T> MvelExpression<T> createExpression(Finder finder, Class<T> resultType, List<String> argumentNames, List<Class<?>> argumentTypes) {
    return new MvelExpression<T>(
        new FParser().parse(finder.value()),
        resultType,
        Iterables.toArray(argumentNames, String.class),
        Iterables.toArray(argumentTypes, Class.class)
    );
  }
}
