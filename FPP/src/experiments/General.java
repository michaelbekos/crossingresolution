package experiments;

/**
 * General class
 * @author Michael A. Bekos
 */
public class General
{
    private y.view.Graph2DView view;

    public General()
    {
        this.view = new y.view.Graph2DView();
    }

    public void run()
    {
        try
        {
            String inputDirectory = "C:/Users/Michael/Downloads/rome-graphml/rome/";
            String outputFileName = "C:/Users/Michael/Desktop/results.csv";

            java.text.DecimalFormat df = new java.text.DecimalFormat("###.##");

            java.io.File dir = new java.io.File(inputDirectory);

            // It is also possible to filter the list of returned files.
            java.io.FilenameFilter filter = new java.io.FilenameFilter()
            {
                public boolean accept(java.io.File file, String name)
                {
                    return true;
                }
            };
            String[] children = dir.list(filter);

            if (children == null)
            {
                // Either dir does not exist or is not a directory
            }
            else
            {
                y.io.GraphMLIOHandler ioh = new y.io.GraphMLIOHandler();

                for (int i=0; i<children.length; i++)
                {

                    view.getGraph2D().clear();
                    ioh.read(view.getGraph2D(), inputDirectory + children[i]);

                    view.fitContent();
                    view.updateView();
                    view.requestFocus();


                    java.lang.StringBuffer buffer = new java.lang.StringBuffer().append(children[i])
                            .append("\t")
                            .append(view.getGraph2D().nodeCount())
                            .append("\t")
                            .append(view.getGraph2D().edgeCount())
                            .append("\t")
                            .append(util.Utilities.maxDegree(view.getGraph2D()))
                            .append("\n");


                    java.io.FileWriter fstream = new java.io.FileWriter(outputFileName, false);
                    java.io.BufferedWriter out = new java.io.BufferedWriter(fstream);

                    fstream = new java.io.FileWriter(outputFileName, true);
                    out = new java.io.BufferedWriter(fstream);
                    out.write(buffer.toString());
                    out.close();
                }
            }
        }
        catch (java.io.IOException exc)
        {
            System.out.println(exc);
        }
    }




    public static void main(String[] args)
    {
        General e = new General();
        e.run();
    }
}

