package graphoperations;

import com.yworks.yfiles.graph.*;

import java.util.ArrayList;

public class VertexStack{
    public ArrayList<Vertex> stack;
    public final int[][] edgeList;
    public ArrayList<Integer> componentStack;
    private IGraph g;

    public VertexStack(IGraph g) {
        this.g = g;
        this.stack = new ArrayList<Vertex>();
        this.edgeList = new int[this.g.getEdges().size()][2];
        this.componentStack = new ArrayList<Integer>();
        int i = 0;
        for (IEdge e : this.g.getEdges()) {
            this.edgeList[i][0] = Integer.parseInt(e.getSourceNode().getTag().toString());
            this.edgeList[i][1] = Integer.parseInt(e.getTargetNode().getTag().toString());
            i++;
        }
    }

    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    public void push(INode node, IGraph g) {
        Vertex vertex = new Vertex(node, this.g);
        this.stack.add(vertex);
    }

    public Vertex pop() {
        if (!this.stack.isEmpty()) {
            Vertex res = this.stack.get(this.stack.size()-1);
            this.stack.remove(this.stack.size()-1);
            return res;
        } else {
            this.stack.clear();
            this.componentStack.clear();
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

    public Vertex(INode node, IGraph g) {
        this.vertex = node;
    }

}
