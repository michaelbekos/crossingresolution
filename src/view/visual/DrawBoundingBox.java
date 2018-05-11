package view.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.IRenderContext;
import com.yworks.yfiles.view.IVisual;

import sidepanel.InitSidePanel;

public class DrawBoundingBox implements IVisual{

    /**
     * Instance Variables
     */
    private GraphComponent view;
    private Color color;


    /**
     * Creates a new instance of EnclosingRectangle by setting the given
     * color as the color of it boundary.
     */
    public DrawBoundingBox(GraphComponent view) {
        this.view = view;
        this.color = Color.black;
    }

    @Override
    public void paint(IRenderContext iRenderContext, Graphics2D graphics2D) {
        double z = view.getZoom();
        double c_x = 1000000;
        double c_y = 1000000;

        if ((int) (100 / z) > 20) { //dont draw for < 20
            graphics2D.setStroke(new BasicStroke((float) (1 / z)));
            graphics2D.setColor(this.color);
            graphics2D.drawLine((int) (0), (int) (0), (int) (c_x), (int) (0));
            graphics2D.drawLine((int) (0), (int) (0), (int) (0), (int) (c_y));
            graphics2D.drawLine((int) (0), (int) (c_y), (int) (c_x), (int) (c_y));
            graphics2D.drawLine((int) (c_x), (int) (0), (int) (c_x), (int) (c_y));
        }
    }
}
