package view.visual;

import com.yworks.yfiles.geometry.*;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.IRenderContext;
import com.yworks.yfiles.view.IVisual;
import com.yworks.yfiles.view.IVisualCreator;

import java.awt.*;

/**
 * Corresponds to a drawable vector.
 *
 * @author  Michael Bekos
 */
public class VectorVisual implements IVisual, IVisualCreator {

    /** Instance Variables */
    private GraphComponent view;
    private PointD vector;
    private INode node;
    private Color color;
    private int thickness;

    /**
     * Creates a new instance of EnclosingRectangle by setting the given
     * color as the color of it boundary.
     */
    public VectorVisual(GraphComponent view, PointD vector, INode node, Color color)
    {
        this.view = view;
        this.vector = new PointD(vector);
        this.vector = PointD.times(100, vector);
        this.node = node;
        this.color = color;
        this.thickness = 1;
    }
    public VectorVisual(GraphComponent view, PointD vector, INode node, Color color, int thickness)
    {
        this.view = view;
        this.vector = new PointD(vector);
        this.vector = PointD.times(100, vector);
        this.node = node;
        this.color = color;
        this.thickness = thickness;
    }

    @Override
    public void paint(IRenderContext iRenderContext, Graphics2D graphics2D) {
        PointD p = node.getLayout().getCenter();
        int bottomLeftX = (int) p.getX();
        int bottomLeftY = (int) p.getY();

        PointD topRight = PointD.add(p, vector);

        int topRightX = (int) topRight.getX();
        int topRightY = (int) topRight.getY();

        graphics2D.setStroke(new BasicStroke(thickness));
        graphics2D.setColor(this.color);
        graphics2D.drawLine(bottomLeftX, bottomLeftY, topRightX, topRightY);

    }

    /**
     * Returns the bounds of this drawable.
     */
    public java.awt.Rectangle getBounds() {
        PointD p = node.getLayout().getCenter();
        int bottomLeftX = (int) p.getX();
        int bottomLeftY = (int) p.getY();


        PointD topRight = PointD.add(p, vector);

        double topRightX = topRight.getX();
        double topRightY = topRight.getY();

        return new java.awt.Rectangle((int) Math.min(bottomLeftX, topRightX),
                (int) Math.min(bottomLeftY, topRightY),
                (int) Math.abs(bottomLeftX - topRight.getX()),
                (int) Math.abs(bottomLeftY - topRight.getY()));
    }


    @Override
    public IVisual createVisual(IRenderContext iRenderContext) {
        return this;
    }

    @Override
    public IVisual updateVisual(IRenderContext iRenderContext, IVisual iVisual) { return this; }
}
