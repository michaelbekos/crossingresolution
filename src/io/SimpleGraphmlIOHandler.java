package io;

import com.yworks.yfiles.algorithms.INodeMap;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.IMapper;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graphml.GraphMLIOHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by Ama on 05.05.2018.
 */
public class SimpleGraphmlIOHandler extends GraphIOHandler{

    static List<String> lines;

    public static void read(IGraph g, String inputFileName) throws  IOException {
        Path path = Paths.get(inputFileName);
        lines = Files.readAllLines(path);

        g.clear();

        int phase = 0;
        int nodesCount = -1;
        int maxNodes = 0;
        int edgesCount = -1;
        int nodesAdded = 0;


        for(String line: lines) {
            line = line.trim();

            if (line.isEmpty() || line.equals("") || line.equals("\n")) {
                continue;
            }else if(line.charAt(1) == 'n') {
                maxNodes++;
            }
        }

        INode[] nodes = new INode[maxNodes];
        String[] nodesId = new String[maxNodes];


        for(String line: lines){
            line = line.trim();

            if(line.isEmpty() || line.equals("") || line.equals("\n")) { continue;}
            else if(line.charAt(1) == 'n') {
                int index = line.indexOf("\"");
                line = line.substring(index+1);
                index = line.indexOf("\"");
                String objString  = line.substring(0,index);
                nodesCount++;
                INode node = g.createNode(new PointD(nodesCount, nodesCount));
                node.setTag(objString);
              //  System.out.println("nodeCount  " +nodesCount);
                nodes[nodesCount] = node;
                nodesId[nodesCount] = objString;

            }
            else if(line.charAt(1) == 'e') {

                int index = line.indexOf("\"");
                line = line.substring(index+1);
                index = line.indexOf("\"");
                String edgeID  = line.substring(0,index);


                line = line.substring(index+1);
                index = line.indexOf("\"");
                line = line.substring(index+1);
                index = line.indexOf("\"");
                String objString  = line.substring(0,index);
                int indexN1 = searchIndex(nodesId, objString);
                if(indexN1 == -1){System.out.println("Maping Node1 failed");}


                line = line.substring(index+1);
                index = line.indexOf("\"");
                line = line.substring(index+1);
                index = line.indexOf("\"");
                objString = line.substring(0,index);
                int indexN2 = searchIndex(nodesId, objString);
                if(indexN1 == -1){System.out.println("Maping Node2 failed");}


                edgesCount++;
                IEdge edge = g.createEdge(nodes[indexN1], nodes[indexN2]);
                edge.setTag(edgeID);

            }
            else {continue;}

        }
    }


    private static int searchIndex(String[] arr, String str){
        int index = -1;
        for(int i = 0; i < arr.length; i++){
            if(arr[i].equals(str)){
                index = i;
                break;
            }
        }
        return index;
    }
    public static void read2(IGraph g, String inputFileName) throws IOException {
        Path path = Paths.get(inputFileName);
        lines = Files.readAllLines(path);

        g.clear();
        int phase = 0;
        int nodesCount = -1;
        int nodesAdded = 0;
        // I HATE THE JAVA COMPILER "mimimi nodes might not have been initialized"
        INode[] nodes = new INode[0];
        for(String line: lines){
            line = line.trim();
            // ignore whitespaces or leading empty lines
            if(line.isEmpty() || line.equals("") || line.equals("\n")) { continue;}
            else if(line.charAt(0) == '#') {continue;}

            // phase 0 contains nodes count
            if(phase == 0){
                nodesCount = Integer.parseInt(line);
                nodes = new INode[nodesCount];
                phase = 1;
            }
            // phase 1 contains all the nodes in the graph
            else if(phase == 1){
                try {
                    int x, y;
                    String[] sxy = line.split("\\s+");
                    x = Integer.parseInt(sxy[0]);
                    y = Integer.parseInt(sxy[1]);
                    INode node = g.createNode(new PointD(x, y));
                    node.setTag(nodesAdded);
                    nodes[nodesAdded] = node;
                    nodesAdded++;
                    if(nodesAdded >= nodesCount) phase = 2;
                } catch(Exception e){}
            }
            // phase 2 contains all the edges in the graph
            else if(phase == 2){
                try {
                    int i1, i2;
                    String[] si1i2 = line.split("\\s+");
                    i1 = Integer.parseInt(si1i2[0]);
                    i2 = Integer.parseInt(si1i2[1]);
                    g.createEdge(nodes[i1], nodes[i2]);
                } catch(Exception e){}
            }
        }
    }

}
