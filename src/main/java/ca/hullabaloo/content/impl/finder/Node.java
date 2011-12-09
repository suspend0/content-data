package ca.hullabaloo.content.impl.finder;

import com.google.common.collect.Sets;

import java.util.Set;

abstract class Node {
  public Set<String> params() {
    Set<String> r = Sets.newHashSet();
    addParams(r);
    return r;
  }

  protected abstract void addParams(Set<String> r);

  public abstract String mvel();
}
