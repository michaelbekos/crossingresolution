package algorithms.graphs;

import java.util.*;

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
    cache_getCrossings = Maybe.nothing();
  }
  
  /* Cache ≃ Maybe
   * - Valid(v) ∼ Just(v)
   * - Invalid  ∼ Nothing
   */
  Maybe<List<Tuple3<LineSegment, LineSegment, Intersection>>> cache_getCrossings;

  // log stuff, if debug enabled
  void debugCacheAccessed(){
    if(G.debug){
      boolean valid = cache_getCrossings.hasValue();
      if(valid) hits++;
      else misses++;
      System.out.println("Cache: " + this);
      System.out.println("Valid? " + valid);
      System.out.println("Hits: " + hits + ", Misses: " + misses);
    }
  }

  public List<Tuple3<LineSegment, LineSegment, Intersection>> getCrossings(IGraph graph, boolean edgesOnly, Maybe<IMapper<INode, PointD>> np){
    debugCacheAccessed();
    // validate cache by lazyly supplying a default value. Nothing happens if cache has a value.
    cache_getCrossings = cache_getCrossings.orElse(() -> Maybe.just(super.getCrossings(graph, edgesOnly, np)));
    // now valid
    return new ArrayList<>(cache_getCrossings.get());
  }

}