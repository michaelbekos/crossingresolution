package view.visual;

import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.utils.IListEnumerable;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.IGraphSelection;
import com.yworks.yfiles.view.ISelectionModel;
import layout.algo.ClinchLayout;
import layout.algo.ClinchLayoutExecutor;
import util.Tuple2;
import util.Util;

import javax.swing.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class InitClinchLayoutExecutor {

    public static void run(GraphComponent view, IGraphSelection selection, JProgressBar progressBar) {
        IGraph graph = view.getGraph();
        ISelectionModel<INode> selectedNodes = selection.getSelectedNodes();

        Set<INode> fixNodes;
        if (selectedNodes.getCount() > 0) {
            fixNodes = graph.getNodes().stream()
                    .filter(node -> !selectedNodes.isSelected(node))
                    .collect(Collectors.toSet());
        } else {
            fixNodes = new HashSet<>(2);
        }

        IListEnumerable<INode> nodes = graph.getNodes();
        Optional<Tuple2<INode, INode>> mostDistantNodes = Util.distinctPairs(nodes.stream(), nodes.stream())
                .filter(pair -> !fixNodes.contains(pair.a) && !fixNodes.contains(pair.b))
                .max(Comparator.comparingDouble(pair -> pair.a.getLayout().getCenter().distanceTo(pair.b.getLayout().getCenter())));

        if (!mostDistantNodes.isPresent()) {
            return;
        }

        INode anchor1 = mostDistantNodes.get().a;
        INode anchor2 = mostDistantNodes.get().b;

        fixNodes.add(anchor1);
        fixNodes.add(anchor2);

        ClinchLayout clinchLayout = new ClinchLayout(graph, anchor1.getLayout().getCenter(), anchor2.getLayout().getCenter(), fixNodes);

        new ClinchLayoutExecutor(view, progressBar, 1000, clinchLayout, 20).run();
    }

}
