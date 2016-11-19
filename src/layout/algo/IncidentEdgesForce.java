package layout.algo;

import util.*;
import java.util.function.Function;
import com.yworks.yfiles.geometry.*;

public class IncidentEdgesForce extends ForceAlgorithm {
  Function<PointD, Function<PointD, Function<Double, Function<Integer, Tuple2<PointD, PointD>>>>> f;
  public IncidentEdgesForce(Function<PointD, Function<PointD, Function<Double, Function<Integer, Tuple2<PointD, PointD>>>>> f1){
    f = f1;
  }
  public Function<PointD, Function<Double, Function<Integer, Tuple2<PointD, PointD>>>> apply(PointD t){
    return f.apply(t);
  }
}