package view.visual;

import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.IRenderContext;
import com.yworks.yfiles.view.IVisual;

import java.awt.*;

public class DrawScale implements IVisual{

    /**
     * Instance Variables
     */
    private GraphComponent view;
    private Color color;


    /**
     * Creates a new instance of EnclosingRectangle by setting the given
     * color as the color of it boundary.
     */
    public DrawScale(GraphComponent view) {
        this.view = view;
        this.color = Color.black;
    }

    @Override
    public void paint(IRenderContext iRenderContext, Graphics2D graphics2D) {
        double z = view.getZoom();
        double c_x = view.getCenter().getX() + ((view.getViewport().getWidth() / 2) - 120 / z);
        double c_y = view.getCenter().getY() + ((view.getViewport().getHeight() / 2) - 34 / z);

        if ((int) (100 / z) > 20) { //dont draw for < 20
            graphics2D.setStroke(new BasicStroke((float) (1 / z)));
            graphics2D.setColor(this.color);
            graphics2D.drawLine((int) (c_x), (int) (c_y), (int) (c_x + 100 / z), (int) (c_y));
            graphics2D.setFont(graphics2D.getFont().deriveFont((float) (12 / z)));
            graphics2D.drawString(Integer.toString((int) (100 / z)), (int) (c_x), (int) (c_y + 15 / z));
        }
    }
}
