package layout.algo;

import util.*;
import java.util.function.Function;
import com.yworks.yfiles.geometry.*;

public class CrossingForce extends ForceAlgorithm {
  Function<PointD, Function<PointD, Function<Double, Tuple2<PointD, PointD>>>> f;
  public CrossingForce(Function<PointD, Function<PointD, Function<Double, Tuple2<PointD, PointD>>>> f1){
    f = f1;
  }
  public Function<PointD, Function<Double, Tuple2<PointD, PointD>>> apply(PointD t){
    return f.apply(t);
  }
}