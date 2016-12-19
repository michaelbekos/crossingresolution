package util;
import java.util.function.Function;

public class Tuple2<A, B>{
  public A a;
  public B b;
  public Tuple2(A a1, B b1){
    a = a1;
    b = b1;
  }
  public <T> Tuple2<T, B> fmapA(Function<A, T> f){
    return new Tuple2<>(f.apply(a), b);
  }
  public <T> Tuple2<A, T> fmapB(Function<B, T> f){
    return new Tuple2<>(a, f.apply(b));
  }
  public <T> Tuple2<A, T> fmap(Function<B, T> f){
    return new Tuple2<>(a, f.apply(b));
  }
  @Override
  public boolean equals(Object o){
    if(o instanceof Tuple2){
      Tuple2 oTup = (Tuple2) o;
      return a.equals(oTup.a) && b.equals(oTup.b);
    }
    return false;
  }
}