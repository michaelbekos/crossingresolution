/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.algo;
import java.util.LinkedList;

/**
 *
 * @author Chrysanthi
 */
public class ExtendTree 
{
    public static void main(String[] args)
    {
        y.base.Graph graph=baseGraph();
//        y.base.Graph newGraph=extendGraph(graph,3);
//        y.base.Node[] nodeList=newGraph.getNodeArray();
//        for (int i=0;i<nodeList.length;i++)
//        {
//            y.base.NodeCursor nc=nodeList[i].neighbors();
//            System.out.print(nodeList[i].index()+":");
//            for (;nc.ok();nc.next())
//            {
//                System.out.print(((y.base.Node) nc.current()).index()+" ");
//            }
//            System.out.println();
//        }
        for (int height=3;height<10;height++)
        {
            y.base.Graph newGraph=extendGraph(graph, height);
            y.base.Node[] nodeList=newGraph.getNodeArray();
            if (nodeList.length==0)
                System.err.print("ERROR for height: "+height);
            for (int i=0;i<nodeList.length;i++)
            {
                y.base.NodeCursor nc=nodeList[i].neighbors();
                System.out.print(nodeList[i].index()+":");
                for (;nc.ok();nc.next())
                {
                    System.out.print(((y.base.Node) nc.current()).index()+" ");
                }
                System.out.println();
            }
            graph=newGraph;
        }
    }
    
    public static y.base.Graph baseGraph()
    {
        int h=2;
        y.base.Graph graph=new y.base.Graph();
        for (int i=0;i<Math.pow(2, h+1)-1;i++)
        {
            graph.createNode();
        }
        for (int i=0;i<Math.pow(2, h+1)-1;i++)
        {
            graph.createNode();
        }
//        for (int i=0;i<Math.pow(2, h+1)-1;i++)
//        {
//            graph.createNode();
//        }
//        for (int i=0;i<Math.pow(2, h+1)-1;i++)
//        {
//            graph.createNode();
//        }
        int [] matching={0,2,1,3};
        y.base.Node[] nodeList=graph.getNodeArray();
        for (int tree=0;tree<nodeList.length/(Math.pow(2,h+1)-1);tree=tree+2)
        {
            for (int pos=0;pos<Math.pow(2,h);pos++)
            {
                graph.createEdge(nodeList[tree*((int) Math.pow(2,h+1)-1)+((int) Math.pow(2,h)-1)+pos], nodeList[(tree+1)*((int) Math.pow(2,h+1)-1)+((int) Math.pow(2,h)-1)+matching[pos]]);
            }
        }
        for (int tree=0;tree<nodeList.length/(Math.pow(2,h+1)-1);tree++)
        {
            int pos=1;
            int source=0;
            while(pos<(Math.pow(2,h+1)-1))
            {
                graph.createEdge(nodeList[tree*((int) Math.pow(2,h+1)-1)+source], nodeList[tree*((int) Math.pow(2,h+1)-1)+pos]);
                graph.createEdge(nodeList[tree*((int) Math.pow(2,h+1)-1)+source], nodeList[tree*((int) Math.pow(2,h+1)-1)+pos+1]);
                source++;
                pos=pos+2;
            }
        }
        return graph;
    }
    
    public static int[][] getBaseMatching(y.base.Graph graph, int height)
    {
        //graph=the graphs to extend, height=the new height
        //the elements of baseMatching are numbered from 0 to 2^(height-1)-1
        y.base.Node[] nodeList=graph.getNodeArray();
        int[][] baseMatching=new int[nodeList.length/((int)(2*(Math.pow(2, height)-1)))][((int)Math.pow(2, height-1))];
        for (int gIndex=0;gIndex<baseMatching.length;gIndex++)
        {
            for (int pos=0;pos<baseMatching[gIndex].length;pos++)
            {
                y.base.Node n=nodeList[gIndex*2*((int) Math.pow(2, height)-1)+ ((int) Math.pow(2, height-1)-1)+pos];
                y.base.NodeCursor nc=n.neighbors();
                for (;nc.ok();nc.next())
                {
                    if (n.index()<((y.base.Node)nc.current()).index())
                    {
                        baseMatching[gIndex][pos]=(((y.base.Node)nc.current()).index()%(2*((int) Math.pow(2, height)-1)))-((int)Math.pow(2, height)-1)-((int)Math.pow(2, height-1)-1);
                    }
                }
            }
        }
        return baseMatching;
    }
    
    public static void addTrees(y.base.Graph graph, int height, int[] matching)
    {
        for (int i=0;i<Math.pow(2, height+1)-1;i++)
        {
            graph.createNode();
        }
        for (int i=0;i<Math.pow(2, height+1)-1;i++)
        {
            graph.createNode();
        }        
        y.base.Node[] nodeList=graph.getNodeArray();
        int trees=nodeList.length/((int) Math.pow(2,height+1)-1);
        for (int tree=trees-2;tree<trees;tree++)
        {
            int pos=1;
            int source=0;
            while(pos<(Math.pow(2,height+1)-1))
            {
                graph.createEdge(nodeList[tree*((int) Math.pow(2,height+1)-1)+source], nodeList[tree*((int) Math.pow(2,height+1)-1)+pos]);
                graph.createEdge(nodeList[tree*((int) Math.pow(2,height+1)-1)+source], nodeList[tree*((int) Math.pow(2,height+1)-1)+pos+1]);
                source++;
                pos=pos+2;
            }
        }
        for (int pos=0;pos<Math.pow(2,height);pos++)
        {
            graph.createEdge(nodeList[(trees-2)*((int) Math.pow(2,height+1)-1)+((int) Math.pow(2,height)-1)+pos], nodeList[(trees-1)*((int) Math.pow(2,height+1)-1)+((int) Math.pow(2,height)-1)+matching[pos]]);
        }
    }
    
    public static void removeTrees(y.base.Graph graph, int height)
    {
        y.base.Node[] nodeList=graph.getNodeArray();
        for (int i=0;i<2*((int)Math.pow(2, height+1)-1);i++)
        {
            graph.removeNode(nodeList[nodeList.length-1-i]);
        }
            
    }
    
    public static int[] createMatching(int[] leftMatching, int[] rightMatching, String permutationNumber)
    {
        int[] matching=new int[leftMatching.length+rightMatching.length];
        for (int i=0;i<leftMatching.length;i++)
            matching[i]=leftMatching[i]*2;
        int[] permutation=generatePermutation(permutationNumber);
        int[] rightMatchingPermuted=new int[rightMatching.length];
        for(int i=0;i<rightMatching.length;i++)
            rightMatchingPermuted[i]=permutation[rightMatching[i]];
        for (int i=rightMatching.length-1;i>=0;i--)
            matching[leftMatching.length+i]=rightMatchingPermuted[i]*2+1;
        return matching;
    }
    
    public static int[] generatePermutation(String permutationNumber)
    {
        int[] permutation=new int[permutationNumber.length()+1];
        for (int i=0;i<permutation.length;i++)
            permutation[i]=i;
        int [] heights=new int[permutationNumber.length()];
        int parent=0;
        int child=1;
        int height=1;
        while(child<heights.length)
        {
            heights[child]=height;
            heights[child+1]=height;
            child=child+2;
            parent=parent+1;
            height=heights[parent]+1;
        }
        for (int i=0;i<permutationNumber.length();i++)
        {
            if (permutationNumber.charAt(i)=='1')
            {
                int tempHeight=heights[i];
                int start=(int)Math.pow(2,tempHeight)-1;
                int pos=i-start;
                int len=permutation.length/((int)Math.pow(2, tempHeight+1));
                for (int j=0;j<len;j++)
                {
                    int temp=permutation[j+len*2*pos];
                    permutation[j+len*2*pos]=permutation[j+len*2*pos+len];
                    permutation[j+len*2*pos+len]=temp;
                }
            }
        }
        return permutation;
    }
    
    public static y.base.Graph extendGraph(y.base.Graph baseGraph, int height)
    {
        //height the new height of the trees
        int[][] baseMatching=getBaseMatching(baseGraph,height);
        y.base.Graph newGraph=new y.base.Graph();
        for (int left=0;left<baseMatching.length;left++)
        {
            int[] leftMatching=baseMatching[left];
            for (int right=left;right<baseMatching.length;right++)
            {
                int[] rightMatching=baseMatching[right];
                for (int number=0;number<((int)Math.pow(2,Math.pow(2,height-1)-height));number++)
                {
                    String tempNumber=Integer.toBinaryString(number);
                    while(tempNumber.length()<Math.pow(2,height-1)-height)
                    {
                        tempNumber="0"+tempNumber;
                    }
                    String permutationNumber="0";
                    int index=1;
                    for (int tempHeight=1;tempHeight<height-1;tempHeight++)
                    {
                        permutationNumber=permutationNumber+"0"+tempNumber.substring(index-1, index-1+((int) Math.pow(2, tempHeight)-1));
                        index=index+((int) Math.pow(2, tempHeight)-1);
                    }
                    
                    int[] matching=createMatching(leftMatching,rightMatching,permutationNumber);
                    addTrees(newGraph, height, matching);
                    if(!maxCycle(newGraph, height))
                    {
                        removeTrees(newGraph,height);
                    }
                }
            }
        }
        return newGraph;
    }
    
    public static boolean maxCycle(y.base.Graph graph, int height)
    {
        y.base.Node[] nodeList=graph.getNodeArray();
        for (int leaf=0;leaf<Math.pow(2, height-1); leaf++)
        {
            boolean acyclic=true;
            boolean[] visited=new boolean[nodeList.length];
            int[] parent=new int[nodeList.length];
            int[] depth=new int[nodeList.length];
            for (int i=0;i<nodeList.length;i++)
            {
                visited[i]=false;
                depth[i]=0;
                parent[i]=-1;
            }
            java.util.LinkedList<y.base.Node> toVisit=new java.util.LinkedList<y.base.Node>();
            toVisit.add(nodeList[nodeList.length-1-leaf]);
            
            while(acyclic && toVisit.getFirst()!=null)
            {
                y.base.Node current=toVisit.poll();
                if (parent[current.index()]>-1)
                    depth[current.index()]=depth[parent[current.index()]]+1;
                else
                    depth[current.index()]=0;
                visited[current.index()]=true;
                y.base.NodeCursor nc=current.neighbors();
                for (;nc.ok() && acyclic;nc.next())
                {
                    if (parent[current.index()]!=((y.base.Node) nc.current()).index())
                    {
                        if(visited[((y.base.Node) nc.current()).index()])
                        {
                            acyclic=false;
                            if (2*depth[current.index()]<2*height+4)
                                return false;
                        }
                        else
                        {
                            parent[((y.base.Node) nc.current()).index()]=current.index();
                            toVisit.add((y.base.Node) nc.current());
                        }
                    }
                }
            }   
        }
        return true;
    }
    
}
