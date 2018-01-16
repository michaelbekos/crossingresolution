package layout.algo.forces;

import java.util.function.Function;
import com.yworks.yfiles.geometry.*;

public class NodePairForce extends ForceAlgorithm {
  Function<PointD, Function<PointD, PointD>> f;
  public NodePairForce(Function<PointD, Function<PointD, PointD>> f1){
    f = f1;
  }
  public Function<PointD, PointD> apply(PointD t){
    return f.apply(t);
  }
}