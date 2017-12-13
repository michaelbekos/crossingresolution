/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.algo;
import java.util.ArrayList;

/**
 *
 * @author Michael
 */
public class MatchingGenerator {
    
    private y.base.Graph graph;
    private y.base.Node[] firstTree;
    private y.base.Node[] secondTree;
    private int height;
    
    public MatchingGenerator(y.base.Graph graph, y.base.Node firstTree[], y.base.Node secondTree[], int height)
    {
        this.graph = graph;
        this.firstTree = firstTree;
        this.secondTree = secondTree;
        this.height = height;
    }
    
    public void addFeasibleMatching()
    {
        int[][] trans={{1,3,4,2},{2,4,3,1},{3,2,1,4},{4,1,2,3}};
        int repeat=height/4;
        int category=height%4;
        int[] startMatching={0};
        
        if (category==1)
        {
            int[] startMatching1={0,1};
            startMatching=startMatching1;
        }
        else if (category==2)
        {
            int[] startMatching1={0,2,1,3};
            startMatching=startMatching1;
        }
        else if (category==3)
        {
            int[] startMatching1={0,5,6,3,4,1,2,7};
            startMatching=startMatching1;
        }
        for (int i=1;i<=repeat;i++)
        {
            int[] newMatching=new int[16*startMatching.length];
            for (int pos=0;pos<newMatching.length;pos++)
            {
                int tree=pos/(4*startMatching.length);
                int tuple=(pos-tree*4*startMatching.length)/4;
                int elem=pos%4;
                newMatching[pos]=(trans[tree][elem]-1)*4*startMatching.length+startMatching[tuple]*4+elem;
            }
            startMatching=newMatching;
        }
        
        
        if (height>6)
        {
            java.util.ArrayList<Integer> l = new java.util.ArrayList<Integer>();
            l.add(0);
            l.add(1);
            l.add(2);
            l.add(3);
            
            
            java.util.List<Integer> list[] = new java.util.List[4];
            algorithms.algo.Permutations<Integer> p[] = new algorithms.algo.Permutations[4];
            p[0] =  new algorithms.algo.Permutations<Integer>(l);
            p[1] =  new algorithms.algo.Permutations<Integer>(l);
            p[2] =  new algorithms.algo.Permutations<Integer>(l);
            p[3] =  new algorithms.algo.Permutations<Integer>(l);
            
            //while (p[0].hasNext())
            //{
                list[0] = l;
                while (p[1].hasNext())
                {
                    list[1] = p[1].next();
                    while (p[2].hasNext())
                    {
                        list[2] = p[2].next();
                        while (p[3].hasNext())
                        {
                            list[3] = p[3].next();
                            
                            int[][] pairing = new int[4][];
                            for (int i = 0; i< pairing.length; i++)
                            {
                                pairing[i] = new int[list[i].size()];
                                for (int j=0; j<list[i].size(); j++)
                                {
                                    pairing[i][j] = list[i].get(j);
                                }
                            }
                            
                            for (int tree=0;tree<4;tree++)
                            {
                                for (int subtree=0;subtree<4;subtree++)
                                {
                                    int[] temp=new int[startMatching.length/16];
                                    int pos=0;
                                    for (int elem=subtree+tree*startMatching.length/4;elem<(tree+1)*startMatching.length/4;elem=elem+4)
                                    {
                                        temp[pos]=startMatching[elem];
                                        pos++;
                                    }               
                                    pos=pairing[tree][subtree]*temp.length/4;
                                    for (int elem=subtree+tree*startMatching.length/4;elem<(tree+1)*startMatching.length/4;elem=elem+4)
                                    {
                                        startMatching[elem]=temp[pos];
                                        pos=(pos+1)%temp.length;
                                    }
                                }
                            }
                            
                            y.base.Edge[] edgesToAdd = new y.base.Edge[(int) Math.pow(2,height)];
                            for (int i = 0; i < (int) Math.pow(2,height); i++)
                            {
                                edgesToAdd[i] = graph.createEdge(firstTree[i+(int) Math.pow(2,height)-1], secondTree[(int) Math.pow(2,height)-1+startMatching[i]]);
                            }
                            
                            int cycleLength = new algorithms.algo.ShortestCycle(graph).computeShortestCycle(); 
                            if (cycleLength >= 2* height + 4)
                            {
                                return;
                            } 
                            
                            for (int i = 0; i < (int) Math.pow(2,height); i++)
                            {
                                graph.removeEdge(edgesToAdd[i]);
                            }                            
                        }
                    }
                //}
            }
        }
    }
    
    public void addFeasibleMatchingRevised()
    {
        int repeat=height/4;
        int category=height%4;
        int[] startMatching={0};
        
        if (category==1)
        {
            int[] startMatching1={0,1};
            startMatching=startMatching1;
        }
        else if (category==2)
        {
            int[] startMatching1={0,3,2,1};
            startMatching=startMatching1;
        }
        else if (category==3)
        {
            int[] startMatching1={0,4,7,3,1,5,6,2};
            //int[] startMatching1={0,5,6,3,4,1,2,7};
            startMatching=startMatching1;
        }
        //augment the graph repeat number of times
        for (int i=1;i<=repeat;i++)
        {
            
            //upper contains {0,2,3,1,0,3,2,1,...} 
            //lower contains {0,3,2,1,0,2,3,1,...}
            int tempHeight=i*4+category;
            int[] newMatching=new int[(int)Math.pow(2, tempHeight)];
            int[] upper=new int[(int)Math.pow(2,tempHeight)];
            int[] lower=new int[(int)Math.pow(2, tempHeight)];
            for (int tuple=0;tuple<Math.pow(2, tempHeight-2);tuple++)
            {
                upper[tuple*4]=0;
                lower[tuple*4]=0;
                upper[tuple*4+3]=1;
                lower[tuple*4+3]=1;
                if (tuple%2==0) //if(tuple%2==0 || height==4)
                {
                    upper[tuple*4+1]=2;
                    lower[tuple*4+1]=3;
                    upper[tuple*4+2]=3;
                    lower[tuple*4+2]=2;
                }
                else
                {
                    upper[tuple*4+1]=3;
                    lower[tuple*4+1]=2;
                    upper[tuple*4+2]=2;
                    lower[tuple*4+2]=3;
                }
            }
            System.out.print("upper: ");
            for (int elem=0;elem<upper.length;elem++)
            {
                System.out.print(upper[elem] +" ");
            }
            System.out.println();
            System.out.print("lower: ");
            for (int elem=0;elem<lower.length;elem++)
            {
                System.out.print(lower[elem] +" ");
            }
            System.out.println();
            for (int upperTree=0;upperTree<4;upperTree++)
                for (int lowerTree=0;lowerTree<4;lowerTree++)
                {
                    if ((upperTree<2 && lowerTree<2) || (upperTree>1 && lowerTree>1))
                    {
                        int[] lowerSub=new int[lower.length/4];
                        for (int k=0;k<lowerSub.length;k++)
                            lowerSub[k]=lower[k+lowerTree*lowerSub.length];
                        int[] lowerPositions=getSubArray(lowerSub,upperTree);
                        int[] upperSub=new int[upper.length/4];
                        for (int k=0;k<upperSub.length;k++)
                            upperSub[k]=upper[k+upperTree*upperSub.length];
                        int[] upperPositions=getSubArray(upperSub,lowerTree);
                        for (int elem=0;elem<startMatching.length;elem++)
                        {
                            //System.out.println((lowerPositions[elem]+lowerTree*lower.length/4) +"--"+(upperPositions[startMatching[elem]]+upperTree*upper.length/4));
                            newMatching[lowerPositions[elem]+lowerTree*lower.length/4]=upperPositions[startMatching[elem]]+upperTree*upper.length/4;
                            graph.createEdge(firstTree[lowerPositions[elem]+lowerTree*lower.length/4+(int) Math.pow(2,height)-1], secondTree[upperPositions[startMatching[elem]]+upperTree*lower.length/4+(int) Math.pow(2,height)-1]);
                        }
                        System.out.println(upperTree+" -- "+lowerTree+": cycle "+new ShortestCycle(graph).computeShortestCycle());
                    }
                    
                }
            boolean found=false;
            int perm=0;
            while (!found && perm <Math.pow(2,Math.pow(2,tempHeight)))
            {
                String binary=Integer.toBinaryString(perm);
                while (binary.length()<Math.pow(2, tempHeight)-1)
                    binary="0"+binary;
                System.out.println(perm+" of "+(Math.pow(2, tempHeight)-1));
                int[] permutation=new int[(int)Math.pow(2,tempHeight)];
                for (int p=0;p<permutation.length;p++)
                    permutation[p]=p;
                for (int exp=0;exp<tempHeight;exp++)
                    for (int pos=0;pos<Math.pow(2, exp);pos++)
                        if (binary.charAt(pos-1+(int)Math.pow(2,exp))=='1')
                        {
                            int length=(int)Math.pow(2,tempHeight-exp-1);
                            int[] temp=new int[length];
                            for (int el=0;el<length;el++)
                            {
                                temp[el]=permutation[pos*2*length+el];
                                permutation[pos*2*length+el]=permutation[(pos*2+1)*length+el];
                                permutation[(pos*2+1)*length+el]=temp[el];
                            }
                        }
                int[]trialMatching=new int[startMatching.length];
                for (int el=0;el<startMatching.length;el++)
                    trialMatching[el]=permutation[startMatching[el]];
                int[]newTrialMatching=newMatching;
                ArrayList<y.base.Edge> edgesToAdd=new ArrayList();
                for (int upperTree=0;upperTree<4;upperTree++)
                    for (int lowerTree=0;lowerTree<4;lowerTree++)
                    {
                        if ((upperTree<2 && lowerTree>1) || (upperTree>1 && lowerTree<2))
                        {
                            int[] lowerSub=new int[lower.length/4];
                            for (int k=0;k<lowerSub.length;k++)
                                lowerSub[k]=lower[k+lowerTree*lowerSub.length];
                            int[] lowerPositions=getSubArray(lowerSub,upperTree);
                            int[] upperSub=new int[upper.length/4];
                            for (int k=0;k<upperSub.length;k++)
                                upperSub[k]=upper[k+upperTree*upperSub.length];
                            int[] upperPositions=getSubArray(upperSub,lowerTree);
                            
                            for (int elem=0;elem<trialMatching.length;elem++)
                            {
                                newTrialMatching[lowerPositions[elem]+lowerTree*lower.length/4]=upperPositions[trialMatching[elem]]+upperTree*upper.length/4;
                                y.base.Edge edge=graph.createEdge(firstTree[lowerPositions[elem]+lowerTree*lower.length/4+(int) Math.pow(2,height)-1],secondTree[upperPositions[trialMatching[elem]]+upperTree*lower.length/4+(int) Math.pow(2,height)-1]);
                                edgesToAdd.add(edge);
                                //graph.createEdge(firstTree[lowerPositions[elem]+lowerTree*lower.length/4+(int) Math.pow(2,height)-1], secondTree[upperPositions[trialMatching[elem]]+upperTree*lower.length/4+(int) Math.pow(2,height)-1]);
                            }
                            //System.out.println(upperTree+" -- "+lowerTree+": cycle "+new ShortestCycle(graph).computeShortestCycle());
                        }
                    }
                int cycle=new ShortestCycle(graph).computeShortestCycle();
                System.out.println(cycle);
                if (cycle<4+2*(tempHeight+4))
                {
                    for (int ed=0;ed<edgesToAdd.size();ed++)
                        graph.removeEdge(edgesToAdd.get(ed));
                    while (edgesToAdd.size()>0)
                        edgesToAdd.remove(edgesToAdd.get(0));
                }
                else
                {
                    found=true;
                    newMatching=newTrialMatching;
                }
                perm++;
            }
            if(!found)
                System.out.println("there is no suitable permutation");
            else
            {
                System.out.print("newMatching: ");
                for (int pos=0;pos<newMatching.length;pos++)
                {
                    System.out.print(newMatching[pos]+" ");
                }
                System.out.println();
                System.out.println(tempHeight+" "+new ShortestCycle(graph).computeShortestCycle());
            }
        }
    }
    
    private int[] getSubArray(int[] array, int element)
    {
        int count=0;
        for (int i=0;i<array.length;i++)
            if (array[i]==element)
                count++;
        int[] subArray=new int[count];
        int index=0;
        for (int i=0;i<array.length;i++)
            if (array[i]==element)
            {
                subArray[index]=i;
                //System.out.print(i+" ");
                index++;
            }
        //System.out.println();
        return subArray;
    }
}
