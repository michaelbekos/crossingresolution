package util;

import com.yworks.yfiles.graph.*;

import java.util.ArrayList;

public class VertexStack{
    public ArrayList<Vertex> stack;
    public ArrayList<Integer> edgesBetweenDeletedNodes;
    public INode[][] verticesAndEdges_tmp;
    public int[][] temp;             //testReinsert
    //TODO remove temp & verticesAndEdges_tmp

    public VertexStack() {
        this.stack = new ArrayList<Vertex>();
        this.edgesBetweenDeletedNodes = new ArrayList<Integer>();
    }

    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    public void push(INode node, IGraph g) {
        Vertex vertex = new Vertex(node, g);
        this.stack.add(vertex);
    }

    public Vertex pop() {
        if (!this.stack.isEmpty()) {
            Vertex res = this.stack.get(this.stack.size()-1);
            this.stack.remove(this.stack.size()-1);
            this.edgesBetweenDeletedNodes.remove(this.edgesBetweenDeletedNodes.size()-1);
            return res;
        } else {
            return null;//todo
        }
    }

    public int size() {
        return this.stack.size();
    }

    public Vertex get(int i ) {
        return this.stack.get(i);
    }

    public boolean contains(INode node, IGraph g) {
        return this.stack.contains(new Vertex(node, g));
    }

}

class Vertex{
    public INode vertex;
    public ArrayList<IEdge> edgeList;

    public Vertex(INode node, IGraph g) {
        this.vertex = node;
        this.edgeList = new ArrayList<IEdge>();
        for (IPort p : this.vertex.getPorts()) {
            for (IEdge e : g.edgesAt(p)) {
                this.edgeList.add(e);
            }
        }
    }

}
