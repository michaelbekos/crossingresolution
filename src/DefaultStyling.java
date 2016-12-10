import com.yworks.yfiles.geometry.SizeD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.styles.PolylineEdgeStyle;
import com.yworks.yfiles.graph.styles.ShinyPlateNodeStyle;
import com.yworks.yfiles.graph.styles.SimpleLabelStyle;
import com.yworks.yfiles.view.Colors;
import com.yworks.yfiles.view.Pen;

import java.awt.*;

/**
 * Created by Jessica Wolz on 10.12.16.
 */
public class DefaultStyling {

    private IGraph graph;

    public DefaultStyling(IGraph graph){

        this.graph = graph;
        defaultEdgeStyle();
        defaultLabelStyle();
        defaultNodeStyle();
    }
    /**
     * Default Node Style
     */
    public void defaultNodeStyle(){
        ShinyPlateNodeStyle defaultNodeStyle = new ShinyPlateNodeStyle();
        defaultNodeStyle.setPaint(Color.RED);
        defaultNodeStyle.setPen(new Pen(Color.GRAY, 1));
        defaultNodeStyle.setShadowDrawingEnabled(false);
        this.graph.getNodeDefaults().setStyle(defaultNodeStyle);
        this.graph.getDecorator().getNodeDecorator().getFocusIndicatorDecorator().hideImplementation();
        this.graph.getNodeDefaults().setSize(new SizeD(17, 17));
    }

    public void defaultEdgeStyle(){

        /* Default Edge Styling */
        PolylineEdgeStyle defaultEdgeStyle = new PolylineEdgeStyle();
        defaultEdgeStyle.setPen(Pen.getBlack());
        this.graph.getEdgeDefaults().setStyle(defaultEdgeStyle);
    }

    public void defaultLabelStyle(){
         /* Default Label Styling */
        SimpleLabelStyle defaultLabelStyle = new SimpleLabelStyle();
        defaultLabelStyle.setFont(new Font("Dialog", Font.PLAIN, 12));
        defaultLabelStyle.setTextPaint(Colors.WHITE);
        this.graph.getNodeDefaults().getLabelDefaults().setStyle(defaultLabelStyle);
        this.graph.getEdgeDefaults().getLabelDefaults().setStyle(defaultLabelStyle);
    }



}
