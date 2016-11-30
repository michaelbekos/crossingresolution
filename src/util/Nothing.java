package util;

import util.Maybe;

public class Nothing<T> extends Maybe<T>{ 
  public boolean hasValue(){ return false; } 
  public T get(){ throw new IllegalStateException(); }
  @Override
  public String toString(){
    return "Nothing";
  }
}