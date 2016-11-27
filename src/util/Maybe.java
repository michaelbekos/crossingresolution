package util;
import util.Just;
import util.Nothing;
import java.util.function.Function;
import java.util.function.Consumer;

public abstract class Maybe<T>{
  public abstract T get();
  public abstract boolean hasValue();
  public <R> Maybe<R> bind(Function<T, Maybe<R>> f){
    if(hasValue()){
      return f.apply(get());
    }
    return Maybe.nothing();
  }
  public <R> Maybe<R> fmap(Function<T, R> f){
    return bind(t -> Maybe.just(f.apply(t)));
  }
  public void andThen(Consumer<T> f){
    if(hasValue()){
      f.accept(get());
    }
  }
  public static <T> Maybe<T> just(T t){
    return new Just<>(t);
  }
  public static <T> Maybe<T> nothing(){
    return new Nothing<>();
  }
  public static <T> Maybe<T> lift(T t){
    return Maybe.just(t);
  }
  public T getDefault(T d){
    if(hasValue()) return get();
    return d;
  }
}
