package layout.algo.forces;

import layout.algo.forces.ForceAlgorithm;

import java.util.function.Function;
import com.yworks.yfiles.geometry.*;

public class NodeNeighbourForce extends ForceAlgorithm {
  Function<PointD, Function<PointD, PointD>> f;
  public NodeNeighbourForce(Function<PointD, Function<PointD, PointD>> f1){
    f = f1;
  }
  public Function<PointD, PointD> apply(PointD t){
    return f.apply(t);
  }
}