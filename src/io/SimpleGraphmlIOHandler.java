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
                String objString  = line.substring(1,index);
                nodesCount++;
                INode node = g.createNode(new PointD(nodesCount, nodesCount));
                node.setTag(objString);
              //  System.out.println("nodeCount  " +nodesCount);
               // System.out.println(objString + "  node");
                nodes[nodesCount] = node;
                nodesId[nodesCount] = objString;

            }
            else if(line.charAt(1) == 'e') {
                edgesCount++;
                int index = line.indexOf("\"");

                /* Test if en edge ID exists */
                String idTest = line.substring(index-3, index);
                String edgeID = edgesCount +"";
               if(idTest.equals("id=")){
                    line = line.substring(index+1);
                    index = line.indexOf("\"");
                    edgeID  = line.substring(0,index);
                    line = line.substring(index+1);
                    index = line.indexOf("\"");
               }


                line = line.substring(index+1);
                index = line.indexOf("\"");
                String objString  = line.substring(1,index);
             //   System.out.println(objString + "  source");
                int indexN1 = searchIndex(nodesId, objString);
                if(indexN1 == -1){System.out.println("Maping Node1 failed");}


                line = line.substring(index+1);
                index = line.indexOf("\"");
                line = line.substring(index+1);
                index = line.indexOf("\"");
                objString = line.substring(1,index);
              //  System.out.println(objString + "  target");
                int indexN2 = searchIndex(nodesId, objString);
                if(indexN1 == -1){System.out.println("Maping Node2 failed");}



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

}
