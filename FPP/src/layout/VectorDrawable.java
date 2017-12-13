package layout;

/**
 * Corresponds to a drawable vector.
 *
 * @author  Michael Bekos
 */

public class VectorDrawable implements y.view.Drawable
{
    /** Instance Variables */
    private y.view.Graph2DView view;
    private y.geom.YVector vector;
    private y.base.Node v;
    private java.awt.Color color;

    /**
     * Creates a new instance of EnclosingRectangle by setting the given
     * color as the color of it boundary.
     */
    public VectorDrawable(y.view.Graph2DView view, y.geom.YVector vector, y.base.Node v, java.awt.Color color)
    {
        this.view = view;
        this.vector = new y.geom.YVector(vector);
        this.vector.scale(100);
        this.v = v;
        this.color = color;
    }

    /**
     * Returns the bounds of this drawable.
     */
    public java.awt.Rectangle getBounds()
    {
        double bottomLeftX = this.view.getGraph2D().getRealizer(v).getCenterX();
        double bottomLeftY = this.view.getGraph2D().getRealizer(v).getCenterY();

        y.geom.YPoint topRight = y.geom.YVector.add(new y.geom.YPoint(bottomLeftX, bottomLeftY), vector);
        
        double topRightX = topRight.x;
        double topRightY = topRight.y;
        
        return new java.awt.Rectangle((int) Math.min(bottomLeftX, topRightX),
                                      (int) Math.min(bottomLeftY, topRightY),
                                      (int) Math.abs(bottomLeftX - topRight.x),
                                      (int) Math.abs(bottomLeftY - topRight.y));
    }

    


    /**
     * Paints itself on the given graphics context.
     */
    public void paint(java.awt.Graphics2D graphic)
    {
        int bottomLeftX = (int) this.view.getGraph2D().getRealizer(v).getCenterX();
        int bottomLeftY = (int) this.view.getGraph2D().getRealizer(v).getCenterY();

        y.geom.YPoint topRight = y.geom.YVector.add(new y.geom.YPoint(bottomLeftX, bottomLeftY), vector);

        int topRightX = (int) topRight.x;
        int topRightY = (int) topRight.y;

        
        graphic.setColor(this.color);
        graphic.drawLine(bottomLeftX, bottomLeftY, topRightX, topRightY);
    }
}
