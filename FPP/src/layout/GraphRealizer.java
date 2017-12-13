package layout;

/**
 *
 * @author michael
 */
public class GraphRealizer {
    
    //Reference to the graph
    private y.view.Graph2DView view;
    
    private int nodeWidth;
    private int nodeHeight;
    private byte nodeShape;
    private java.awt.Color fillColor;
    private java.awt.Color fillColor2;
    private java.awt.Color lineColor;
    
    public GraphRealizer(y.view.Graph2DView view)
    {
        this.view = view;
        
        this.nodeWidth = 20;
        this.nodeHeight = 20;
        this.nodeShape = y.view.ShapeNodeRealizer.RECT;
        this.fillColor = java.awt.Color.LIGHT_GRAY;
        this.fillColor2 = java.awt.Color.LIGHT_GRAY;
        this.lineColor = java.awt.Color.BLACK;
    }
    
    public int getNodeWidth()
    {
        return this.nodeWidth;
    }
    
    public int getNodeHeight()
    {
        return this.nodeHeight;
    }
    
    public byte getNodeShape()
    {
        return this.nodeShape;
    }
    
    public java.awt.Color getFillColor()
    {
        return this.fillColor;
    }
    
    public java.awt.Color getFillColor2()
    {
        return this.fillColor2;
    }

    
    public java.awt.Color getLineColor()
    {
        return this.lineColor;
    }
    
    public void setNodeWidth(int nodeWidth)
    {
        this.nodeWidth = nodeWidth;
    }
    
    public void setNodeHeight(int nodeHeight)
    {
        this.nodeHeight = nodeHeight;
    }
    
    public void setNodeShape(byte nodeShape)
    {
        this.nodeShape = nodeShape;
    }
    
    public void setFillColor(java.awt.Color fillColor)
    {
        this.fillColor = fillColor;
    }
    
    public void setFillColor2(java.awt.Color fillColor2)
    {
        this.fillColor2 = fillColor2;
    }
    
    public void setLineColor(java.awt.Color lineColor)
    {
        this.lineColor = lineColor;
    }
    
    public void updateGraph()
    {
        for (y.base.NodeCursor nc = this.view.getGraph2D().nodes(); nc.ok(); nc.next())
        {
            this.updateNode(nc.node());
        }
    }

    public void updateNode(y.base.Node v)
    {
        y.view.Graph2D graph = view.getGraph2D();

        graph.getRealizer(v).setWidth(this.nodeWidth);
        graph.getRealizer(v).setHeight(this.nodeHeight);

        ((y.view.ShapeNodeRealizer) graph.getRealizer(v)).setShapeType(this.nodeShape);

        graph.getRealizer(v).setFillColor(this.fillColor);
        graph.getRealizer(v).setFillColor2(this.fillColor2);
        graph.getRealizer(v).setLineColor(this.lineColor);
        this.view.updateView();
    }
}
