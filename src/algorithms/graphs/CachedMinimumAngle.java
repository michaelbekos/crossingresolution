package algorithms.graphs;

import java.util.*;

import com.sun.istack.internal.Nullable;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.geometry.PointD;

import util.*;
import util.graph2d.*;

public class CachedMinimumAngle extends MinimumAngle.MinimumAngleHelper {
  int hits = 0, misses = 0;
  
  public CachedMinimumAngle(){
    invalidate();
  }

  // reset cache
  public void invalidate(){
    cache_getCrossings = null;
  }
  
  /* Cache ≃ Maybe
   * - Valid(v) ∼ Just(v)
   * - Invalid  ∼ Nothing
   */
  @Nullable
  List<Tuple3<LineSegment, LineSegment, Intersection>> cache_getCrossings;

  // log stuff, if debug enabled
  void debugCacheAccessed(){
    if(G.debug){
      boolean valid = cache_getCrossings != null;
      if(valid) hits++;
      else misses++;
      System.out.println("Cache: " + this);
      System.out.println("Valid? " + valid);
      System.out.println("Hits: " + hits + ", Misses: " + misses);
    }
  }

  public List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossings(IGraph graph, boolean edgesOnly, Maybe<IMapper<INode, PointD>> np){
    debugCacheAccessed();

    if (cache_getCrossings == null) {
      cache_getCrossings = super.getCrossings(graph, edgesOnly, np);
    }

    return new ArrayList<>(cache_getCrossings);
  }

}