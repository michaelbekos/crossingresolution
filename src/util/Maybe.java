package util;
import util.Just;
import util.Nothing;


public abstract class Maybe<T>{
  public abstract T get();
  public abstract boolean hasValue();
  public <R> Maybe<R> bind(java.util.function.Function<T, Maybe<R>> f){
    if(hasValue()){
      return f.apply(get());
    }
    return Maybe.nothing();
  }
  public static <T> Maybe<T> just(T t){
    return new Just<>(t);
  }
  public static <T> Maybe<T> nothing(){
    return new Nothing<>();
  }
}
