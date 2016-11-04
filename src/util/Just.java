package util;

import util.Maybe;

public class Just<T> extends Maybe<T>{
  T t;
  public Just(T t1){
    t = t1;
  }
  public boolean hasValue(){ return true; }
  public T get(){ return t; }
}