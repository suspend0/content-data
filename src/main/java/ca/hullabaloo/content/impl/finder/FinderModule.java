package ca.hullabaloo.content.impl.finder;

import ca.hullabaloo.content.api.Finder;
import ca.hullabaloo.content.api.Finders;
import ca.hullabaloo.content.api.Storage;
import com.google.inject.AbstractModule;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;

public final class FinderModule extends AbstractModule {
  @Override
  protected void configure() {
    bindListener(new AbstractMatcher<TypeLiteral<?>>() {
      @Override
      public boolean matches(TypeLiteral<?> typeLiteral) {
        return Finders.class.isAssignableFrom(typeLiteral.getRawType());
      }
    }, new FinderTypeListener());
    if (currentStage() != Stage.PRODUCTION) {
      bindListener(Matchers.any(), new FinderAnnotationVerifier());
    }
  }

  private class FinderTypeListener implements TypeListener {
    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
      for (final Method m : type.getRawType().getMethods()) {
        if (m.isAnnotationPresent(Finder.class)) {
          if (m.getReturnType() == Iterator.class) {
            try {
              Call call = new Call(m, encounter.getProvider(Storage.class));
              FinderMethodInterceptor interceptor = new FinderMethodInterceptor(call);
              encounter.bindInterceptor(Matchers.identicalTo(m), interceptor);
            } catch (ParseException e) {
              encounter.addError("Could not parse finder expression on %s: %s", m, e.getMessage());
            }
          } else if (m.getDeclaringClass() != Object.class) {
            encounter.addError("%s methods must return %s",
                Finder.class.getSimpleName(), Iterator.class.getSimpleName());
          }
        }
      }
    }
  }

  private static class FinderMethodInterceptor implements MethodInterceptor {
    private final Call call;

    public FinderMethodInterceptor(Call call) {
      this.call = call;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      return this.call.invoke(invocation.getArguments());
    }
  }

  private static class FinderAnnotationVerifier implements TypeListener {
    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
      verify(type.getRawType(), encounter);
    }

    private void verify(Class<?> type, TypeEncounter<?> encounter) {
      if (type == Object.class) {
        return;
      }

      for (Method method : type.getMethods()) {
        if (method.isAnnotationPresent(Finder.class)) {
          if (!Finders.class.isAssignableFrom(type)) {
            encounter.addError("method %s on type without %s", method, Finders.class);
          }
          if (Modifier.isFinal(method.getModifiers())
              || Modifier.isStatic(method.getModifiers())
              || Modifier.isPrivate(method.getModifiers())) {
            encounter.addError("%s method %s cannot be annotated with %s",
                Modifier.toString(method.getModifiers()), method, Finder.class);
          }
        }
      }

      verify(type.getSuperclass(), encounter);
    }
  }
}
