package util;
import java.util.function.*;

public abstract class Either<A, B>{
  public abstract boolean isLeft();
  public abstract boolean isRight();
  public abstract A getLeft();
  public abstract B getRight();
  public abstract <C> C match(Function<A, C> mLeft, Function<B, C> mRight);
  public abstract void match(Consumer<A> mLeft, Consumer<B> mRight);

  public static <A, B> Either<A, B> left(A a){
    return new Left<>(a);
  }
  public static <A, B> Either<A, B> right(B b){
    return new Right<>(b);
  }
  static class Left<A, B> extends Either<A, B>{
    A a;
    public Left(A a1){
      a = a1;
    }
    public boolean isLeft(){
      return true;
    }
    public boolean isRight(){
      return false;
    }
    public A getLeft(){
      return a;
    }
    public B getRight(){
      throw new IllegalStateException(); 
    }
    public <C> C match(Function<A, C> mLeft, Function<B, C> mRight){
      return mLeft.apply(a);
    }
    public void match(Consumer<A> mLeft, Consumer<B> mRight){
      mLeft.accept(a);
    }

  }
  static class Right<A, B> extends Either<A, B>{
    B b;
    public Right(B b1){
      b = b1;
    }
    public boolean isLeft(){
      return false;
    }
    public boolean isRight(){
      return true;
    }
    public A getLeft(){
      throw new IllegalStateException(); 
    }
    public B getRight(){
      return b;
    }
    public <C> C match(Function<A, C> mLeft, Function<B, C> mRight){
      return mRight.apply(b);
    }
    public void match(Consumer<A> mLeft, Consumer<B> mRight){
      mRight.accept(b);
    }
  }
}