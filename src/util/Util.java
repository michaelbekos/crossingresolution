package util;

import java.util.*;
import java.util.stream.*;

public abstract class Util {

  public static <A, B> Stream<Tuple2<A, B>> pairs(Stream<A> as, Stream<B> bs){
    List<B> bsList = bs.collect(Collectors.toList());
    return as.flatMap(a -> bsList.stream().map(b -> new Tuple2<>(a, b)));
  }
  public static <A> Stream<Tuple2<A, A>> distinctPairs(Stream<A> as, Stream<A> bs){
    List<Tuple2<A, A>> res = new LinkedList<>();
    List<A> asList = as.collect(Collectors.toList());
    List<A> bsList = bs.collect(Collectors.toList());
    Set<A> seenAs = new HashSet<>();
    for(A a: asList){
      seenAs.add(a);
      for(A b: bsList){
        if(seenAs.contains(b)) continue;
        res.add(new Tuple2<>(a, b));
      }
    }
    return res.stream();
  }
}