package layout;

import algorithms.algo.MatchingGenerator;
import y.base.EdgeCursor;

/**
 * Corresponds to a drawable ply-circle.
 *
 * @author  Michael Bekos
 */

public class PlyCircleDrawable implements y.view.Drawable
{
    /** Instance Variables */
    private y.view.Graph2DView view;
    private y.base.Node u;
    private java.awt.Color color;

    /**
     * Creates a new instance of PlyCircleDrawable by setting the given
     * color as the color of it boundary.
     */
    public PlyCircleDrawable(y.view.Graph2DView view, y.base.Node u, java.awt.Color color)
    {
        this.view = view;
        this.u = u;
        this.color = color;
    }

    /**
     * Returns the bounds of this drawable.
     */
    public java.awt.Rectangle getBounds()
    {
        int x1 = (int) this.view.getGraph2D().getRealizer(u).getCenterX();
        int y1 = (int) this.view.getGraph2D().getRealizer(u).getCenterY();

        double radius = 0;
        for (EdgeCursor ec = this.u.edges(); ec.ok(); ec.next())
        {
            int x2 = (int) this.view.getGraph2D().getRealizer(ec.edge().source() != u ? ec.edge().source() : ec.edge().target()).getCenterX();
            int y2 = (int) this.view.getGraph2D().getRealizer(ec.edge().source() != u ? ec.edge().source() : ec.edge().target()).getCenterY();
            if (radius < Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)))
            {
                radius = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
            }
        }

        return new java.awt.Rectangle(x1-(int)radius/2, y1-(int)radius/2, (int) radius, (int) radius);
    }

    /**
     * Paints itself on the given graphics context.
     */
    public void paint(java.awt.Graphics2D graphic)
    {
        java.awt.Rectangle r = this.getBounds();
        graphic.setColor(this.color);
        graphic.drawOval(r.x, r.y, r.width, r.height);
    }
}
