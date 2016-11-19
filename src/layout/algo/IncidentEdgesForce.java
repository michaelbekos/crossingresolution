package layout.algo;

public class IncidentEdgesForce extends ForceAlgorithm {
  Function<PointD, Function<PointD, Function<Double, Tuple2<PointD, PointD>>>> f;
  public IncidentEdgesForce(Function<PointD, Function<PointD, Function<Double, Tuple2<PointD, PointD>>>> f1){
    f = f1;
  }
  public Function<PointD, Function<Double, Tuple2<PointD, PointD>>> apply(PointD t){
    return f.apply(t);
  }
}