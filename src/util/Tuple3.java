package util;

public class Tuple3<A, B, C>{
    public A a;
    public B b;
    public C c;
    public Tuple3(A a1, B b1, C c1){
      a = a1;
      b = b1;
      c = c1;
    }
    public Tuple3(Tuple2<A, B> ab, C c1){
      a = ab.a;
      b = ab.b;
      c = c1;
    }
    public <D> Tuple3(Tuple3<A, B, D> abc, C c1){
      a = abc.a;
      b = abc.b;
      c = c1;
    }
  }