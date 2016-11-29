package util;

import util.Maybe;

public class Just<T> extends Maybe<T>{
  T t;
  public Just(T t1){
    if(t1 == null){
      throw new IllegalStateException();
    }
    t = t1;
  }
  public boolean hasValue(){ return true; }
  public T get(){ return t; }
  @Override
  public String toString(){
    return "Just(" + t + ")";
  }
}