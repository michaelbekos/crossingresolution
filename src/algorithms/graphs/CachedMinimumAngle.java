package algorithms.graphs;

import com.sun.istack.internal.Nullable;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.IMapper;
import com.yworks.yfiles.graph.INode;
import util.G;
import util.graph2d.Intersection;

import java.util.ArrayList;
import java.util.List;

public class CachedMinimumAngle extends MinimumAngle.MinimumAngleHelper {
  int hits = 0, misses = 0;
  
  public CachedMinimumAngle(){
    invalidate();
  }

  // reset cache
  public void invalidate(){
    cache_getCrossings = null;
  }
  
  @Nullable
  List<Intersection> cache_getCrossings;

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

  public List<Intersection> getCrossings(IGraph graph, boolean edgesOnly, @Nullable IMapper<INode, PointD> np){
    debugCacheAccessed();

    if (cache_getCrossings == null) {
      cache_getCrossings = super.getCrossings(graph, edgesOnly, np);
    }

    return new ArrayList<>(cache_getCrossings);
  }

}