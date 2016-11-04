package util;

public abstract class Maybe<T>{
  public abstract T get();
  public abstract boolean hasValue();
}
