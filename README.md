# Crossing Resolution
This repository provides algorithms that allow drawing of 2D graphs in a fashion that maximizes the minimum crossing angle of their
edges. It is developed in the scope of the [Graph Drawing Contest](http://www.graphdrawing.de/contest2017/contest.html)
2017 and 2018.

## Installation and Dependencies
Currently there exists no binary version of the program. Thus you need a [Java SDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
(Java 8 or higher) installed in order to run it. Additionally you need a version of [yFiles for Java 3.1](http://www.yworks.com/products/yfiles-for-java)
and a valid license file. Put the `*.jar` and license files into the `src` directory and add the `jar` to the classpath.

## Usage
You can run the program by compiling and executing the `main/Main` class or simply running the scripts located in
`src/scripts`. A window will appear showing an empty panel for the graph and a sidebar where all the algorithms can be
accessed. Load a graph via `File->Open` or `File->Open Contest File`. The first loads graphs saved as `*.graphml`. The 
latter opens graphs in the contest file format.

### Algorithms
On the sidepanel there are a number of algorithms available, each configurable by a number of parameters:
* Random Movement: A pretty simple algorithm that randomly moves nodes to new locations that do not hurt the minimum angle.
* Force Algorithm: An implementation of a force-directed algorithm supplying a number of forces designed to increase the
minimum angle. Each can be enabled and disabled by their respective check-boxes.
* Genetic Algorithm: An algorithm that uses a genetic approach to try and optimize parameters of the Force Algorithm. It is
used very rarely as it is quite slow and does not produce good results.
* Clinch Nodes: An algorithm that tries to move nodes closer to a imaginary line calculated by the two most distant nodes.
Originally meant as a post-processing step for an algorithm that is not yet implemented.
* Gridding: An algorithm that tries to move nodes with arbitrary locations to nearby integer positions, thus making the
graph valid for the contest.
* Misc.: A tab that mainly provides a collection of yFiles algorithms plus an FPP implementation that draws planar graphs.

In practice, a yFiles Organic Layout followed by Random Movement has proven to produce the best results.

In addition to the algorithms, each tab provides some buttons which allow modifications of the graph:
* Show Best: Resets the graph to the best result achieved since loading.
* Scale Me to the Box: Scales the graph to the maximum box size allowed by the contest.
* Remove All Chains/Reinsert One/All Chains: Allows removing and reinserting all nodes of degree two.
* Graph Info: Shows detailed information about the graph such as dimensions and degrees of the nodes.

### CLI Usage
It is also possible to open multiple frames at the same time by supplying some CLI parameters to the `main/Main` program.
Each frame will load one graph and start the Random Movement algorithm after a yFiles Organic Layout. For details, run
the program from command line with the `--help` flag. 

## Code Details
Here is a quick overview over the packages:
* `algorithms.graphs`: Contains some general graph algorithms.
* `graphoperations`: Contains classes necessary for graph operations like node removal and scaling.
* `io`: Contains classes for loading and saving the graphs
* `layout.algo`: Contains all algorithms and necessary utility classes
* `main`: Contains the main parts of the view.
* `randomgraphgenerators`: Contains classes that are able to create new random graphs with certain properties.
* `sidepanel`: Contains all sidepanel related code, especially all the sidepanel `LayoutInterfaceItems`.
* `util`: Contains some utility classes.
* `view.visual`: Contains classes for displaying some visuals like debug vectors
* `yfilesadapter`: Contains an adapter to run an arbitrary yFiles layout as `ILayout`

### Adding new algorithms
YFiles uses two different graph representations: `IGraph`, which is meant for the view part of the library and `LayoutGraph`,
which is used by the yFiles layout algorithms. Although it would be somehow cleaner to work on yFiles `LayoutGraph`s, we
decided to directly use the `IGraph` as it makes a lot of code easier and no adapters are necessary. However, algorithms
should NOT directly move nodes of the `IGraph` as this will update the view immediately and result in severe performance
loss. Instead, work on an instance of `Mapper<INode, PointD>`, which can be obtained from `layout.algo.utils.PositionMap.fromIGraph`.

Adding a new algorithm requires three things: 
* A class that implements the `layout.algo.execution.ILayout` interface. This should contain the logic of the algorithm. Implementing
this interface allows it to be run by a `layout.algo.execution.(Basic)IGraphLayoutExecutor`.
* A class that implements `layout.algo.layoutinterface.IConfigurator`. This should contain a set of parameters that configure
the algorithm. The parameters are represented by so-called `LayoutInterfaceItems` (if you know a better name, feel free
to change it), which enable abstract communication with an arbitrary view. They are constructed by a
`layout.algo.layoutinterface.ILayoutInterfaceItemFactory` within the `init` method of `ILayoutConfigurator`.
* Adding the algorithm to the view: Most usually you want to create a new tab for this. This should be done within the
`sidepanel.InitSidePanel#initSidePanel()` method using `sidepanel.InitSidePanel#addAlgorithm()`.

### General Things
When contributing code to this repository please try and keep the code as *simple, clear, readable and well-structured*
as possible. Thus
* Follow the usual Java naming and code-style conventions
* Avoid duplicate code (search through existing code before writing code that already exists)
* Make good use of both the Java standard library and the yFiles library
* Keep files/classes/methods small, use packages for structuring
* Keep logic and view as separate as possible
* Use OOP/imperative style where appropriate and use functional style where appropriate
* Test your code thoroughly before commiting (currently no unit tests exist, but it might be advisable to add some in
the near future)

### Useful links
* [yFiles for Java Documentation](http://docs.yworks.com/yfilesjava/doc/api/#/home)
