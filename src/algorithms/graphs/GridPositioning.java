/**
 * Created by Jessica Wolz on 01.12.16.
 */


import com.yworks.yfiles.geometry.*;
import com.yworks.yfiles.graph.*;

import java.util.*;

import layout.algo.ForceAlgorithmApplier;
import util.*;
import algorithms.graphs.*;

public class GridPositioning {

    private IGraph graph;
    private static IMapper<INode, PointD> nodePositions;

    public GridPositioning(IGraph graph){
        this.graph = graph;
        nodePositions = ForceAlgorithmApplier.initPositionMap(this.graph);
    }

    public IMapper<INode, PointD> getGridNodes(){

        for(INode u : graph.getNodes()){
            PointD currP = nodePositions.getValue(u);
            double currAngle = getResultingAngle(nodePositions);
            PointD leftDown, leftUp, rightDown, rightUp;
            // four different grid positions of same node
            List<PointD> gridPoints = new ArrayList<>();
            gridPoints.add(new PointD(Math.floor(currP.getX()), Math.floor(currP.getY())));
            gridPoints.add(new PointD(Math.floor(currP.getX()), Math.ceil(currP.getY())));
            gridPoints.add(new PointD(Math.ceil(currP.getX()), Math.floor(currP.getY())));
            gridPoints.add(new PointD(Math.ceil(currP.getX()), Math.ceil(currP.getY())));

           /* leftDown = new PointD(Math.floor(currP.getX()), Math.floor(currP.getY()));
            leftUp = new PointD(Math.floor(currP.getX()), Math.ceil(currP.getY()));
            rightDown = new PointD(Math.ceil(currP.getX()), Math.floor(currP.getY()));
            rightUp = new PointD(Math.ceil(currP.getX()), Math.ceil(currP.getY()));*/

            IMapper<INode, PointD> temp = ForceAlgorithmApplier.initPositionMap(this.graph);
            List<Tuple2<PointD, Double>> coord = new ArrayList<>();
            //double x;
            Iterator<PointD> iter = gridPoints.iterator();
            while (iter.hasNext())
            {
                PointD it = iter.next();
                coord.add(new Tuple2<>(it, getResultingAngle(temp, u, it)));
            }

            /*if((x = getResultingAngle(temp, u, leftDown)) > currAngle){
                coord.add(new Tuple2<>(leftDown, x));
            }
            if((x = getResultingAngle(temp, u, leftUp)) > currAngle){
                coord.add(new Tuple2<>(leftUp, x));
            }
            if((x = getResultingAngle(temp, u, rightDown)) > currAngle){
                coord.add(new Tuple2<>(rightDown, x));
            }
            if((x = getResultingAngle(temp, u, rightUp)) > currAngle){
                coord.add(new Tuple2<>(leftDown, x));
            }*/

            Comparator<Tuple2<PointD, Double>> byAngle = (p1, p2) -> p1.b.compareTo(p2.b);
            Collections.sort(coord, byAngle);
            if(coord.size() > 0){
                // TODO: ascending or descending comparator!?
                // double newAngle = coord.get(0);
                PointD newP = coord.get(coord.size()).a;
                nodePositions.setValue(u, newP);

            } else {
                break;
            }

        }

        return nodePositions;
    }

    public Double getResultingAngle(IMapper<INode, PointD> map, INode node, PointD p){
        map.setValue(node, p);
        return getResultingAngle(map);
    }

    public Double getResultingAngle(IMapper<INode, PointD> map){

        Maybe<Double> tempAngle = MinimumAngle.getMinimumAngle(this.graph, Maybe.just(map));
        if(tempAngle.hasValue()) {
            return tempAngle.get();
        }
        return 0.0;
    }

    public boolean isGridded(IGraph graph){
        for(INode u: graph.getNodes()){
            if((u.getLayout().getCenter().getX() % 1) != 0){
                return false;
            }
            if((u.getLayout().getCenter().getY() % 1) != 0){
                return false;
            }
        }
        return true;
    }
}
