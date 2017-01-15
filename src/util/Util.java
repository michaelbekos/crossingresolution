package util;

import java.util.*;
import java.util.stream.*;

import com.yworks.yfiles.utils.IListEnumerable;

public abstract class Util {

  public static <A, B> Stream<Tuple2<A, B>> pairs(Stream<A> as, Stream<B> bs){
    List<B> bsList = bs.collect(Collectors.toList());
    return as.flatMap(a -> bsList.stream().map(b -> new Tuple2<>(a, b)));
  }

  public static <T1, T2> Stream<Tuple2<T1, T2>> nonEqalPairs(Stream<T1> s1, Stream<T2> s2){
    return pairs(s1, s2).filter(t12 -> !t12.a.equals(t12.b));
  }

  public static <A> Stream<Tuple2<A, A>> distinctPairs(IListEnumerable<A> as){
    return distinctPairs(as, 0);
  }
  public static <A> Stream<Tuple2<A, A>> distinctPairs(IListEnumerable<A> as, int skip){
    Stream<Tuple2<A, A>> res = Stream.empty();
    while(as.size() != skip){
      A head = as.getItem(skip);
      Stream<A> tail = as.stream().skip(skip);
      res = Stream.concat(res, tail.map(a -> new Tuple2<>(head, a)));
      skip++;
    }
    return res;
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