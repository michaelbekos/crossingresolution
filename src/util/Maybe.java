package util;
import java.util.function.*;

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
  public T getDefault(T t){
    if(hasValue()) return get();
    return t;
  }
  public T getDefault(Supplier<T> lazyT){
    if(hasValue()) return get();
    return lazyT.get();
  }
  public Maybe<T> orElse(T t){
    if(hasValue()) return this;
    return Maybe.just(t);
  }
  public Maybe<T> orElse(Supplier<T> lazyT){
    if(hasValue()) return this;
    return Maybe.just(lazyT.get());
  }
  public static <T> Consumer<Maybe<T>> lift(Consumer<T> f){
    return (m -> m.andThen(el -> f.accept(el)));
  }
  public static <T, R> Function<Maybe<T>, Maybe<R>> lift(Function<T, R> f){
    return (m -> m.fmap(el -> f.apply(el)));
  }



  static class Just<T> extends Maybe<T> {
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
      return "(Just " + t + ")";
    }
  }

  static class Nothing<T> extends Maybe<T> { 
    public boolean hasValue(){ return false; } 
    public T get(){ throw new IllegalStateException(); }
    @Override
    public String toString(){
      return "Nothing";
    }
  }
}
