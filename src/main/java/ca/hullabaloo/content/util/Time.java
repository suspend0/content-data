package ca.hullabaloo.content.util;

public abstract class Time {
  public abstract long now();

  public static Time systemTime() {
    return SYSTEM_TIME;
  }

  private static final Time SYSTEM_TIME = new Time() {
    @Override
    public long now() {
      return System.currentTimeMillis();
    }
  };
}
