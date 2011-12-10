package ca.hullabaloo.content.util;

/** Like a javadoc at-see, but stable thorough refactoring and can be found-usages */
public @interface See {
  Class ref();
  String note() default "";
}
