package ca.hullabaloo.content.api;

/**
 * Created by IntelliJ IDEA.
 * User: darren
 * Date: 11/11/11
 * Time: 11:18 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Updater<T> {
  T fields();

  Updater<T> forId(int id);

  <V> Updater<T> set(V field, V value);
}
