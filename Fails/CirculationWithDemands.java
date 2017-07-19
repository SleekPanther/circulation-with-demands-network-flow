import java.util.*;

public class CirculationWithDemands {

	class Edge{
		private static final int defaultEdgeCapacity = 1;
		private int fromVertex;		//an edge is composed of 2 vertices
		private int toVertex;
		private int capacity;		//edges also have a capacity & a flow
		private int flow;

		//Overloaded constructor to create a generic edge with a default capacity
		public Edge(int fromVertex, int toVertex){
			this(fromVertex, toVertex, defaultEdgeCapacity);
		}
		public Edge(int fromVertex, int toVertex, int capacity){
			this.fromVertex = fromVertex;
			this.toVertex = toVertex;
			this.capacity = capacity;
		}
		
		//Given an end-node, Returns the other end-node (completes the edge)
		public int getOtherEndNode(int vertex){
			if(vertex==fromVertex){
				return toVertex;
			}
			return fromVertex;
		}
		
		public int getCapacity(){
			return capacity;
		}
		
		public int getFlow(){
			return flow;
		}
		
		public int residualCapacityTo(int vertex){
			if(vertex==fromVertex){
				return flow;
			}
			return (capacity-flow);
		}
		
		public void increaseFlowTo(int vertex, int changeInFlow){
			if(vertex==fromVertex){
				flow = flow-changeInFlow;
			}
			else{
				flow = flow+changeInFlow;
			}
		}
		
		//Prints edge using Array indexes, not human readable ID's like "S" or "T"
		@Override
		public String toString(){
			return "(" + fromVertex+" --> "+toVertex + ")";
		}
	}


	private ArrayList<ArrayList<Edge>> graph;		//Graph is represented as an ArrayList of Edges
	private ArrayList<String> getStringVertexIdFromArrayIndex;	//convert between array indexes (starting from 0) & human readable vertex names
	private int vertexCount;		//How many vertices are in the graph

	//These fields are updated by fordFulkersonMaxFlow and when finding augmentation paths
	private Edge[] edgeTo;
	private boolean[] isVertexMarked;		//array of all vertices, updated each time an augmentation path is found
	private int flow;

	//Constructor initializes graph edge list with number of vertexes, string equivalents for array indexes & adds empty ArrayLists to the graph for how many vertices ther are
	public CirculationWithDemands(int vertexCount, ArrayList<String> getStringVertexIdFromArrayIndex){
		this.vertexCount = vertexCount;
		this.getStringVertexIdFromArrayIndex = getStringVertexIdFromArrayIndex;

		graph = new ArrayList<>(vertexCount);		//Populate graph with empty ArrayLists for each vertex
		for(int i=0; i<vertexCount; ++i){
			graph.add(new ArrayList<>());
		}
	}

	public void addEdge(int fromVertex, int toVertex, int capacity){
		Edge newEdge = new Edge(fromVertex, toVertex, capacity);	//create new edge between 2 vertices
		graph.get(fromVertex).add(newEdge);
	// graph.get(toVertex).add(newEdge);
	}

	//Finds max flow / min cut of a graph
	public void fordFulkersonMaxFlow(int source, int sink){
		edgeTo = new Edge[vertexCount];
		while(existsAugmentingPath(source, sink)){
			int flowIncrease = Integer.MAX_VALUE;	//default value is 1 since it's a bipartite matching problem with capacities = 1
			
			System.out.print("Matched Vertices  "+(getStringVertexIdFromArrayIndex.get( edgeTo[sink].getOtherEndNode(sink) ) )+" & ");		//Print the 1st vertex of the pair
			//Loop over The path from source to sink. (Update max flow & print the other matched vertex)
			for(int i=sink; i!=source; i=edgeTo[i].getOtherEndNode(i)){
				//Loop stops when i reaches the source, so print out the vertex in the path that comes right before the source
				if(edgeTo[i].getOtherEndNode(i)==source){
					System.out.print(getStringVertexIdFromArrayIndex.get(i));		//use human readable vertex ID's
				}
				flowIncrease = Math.min(flowIncrease, edgeTo[i].residualCapacityTo(i));
			}
			
			//Update Residual Capacities
			for(int i=sink; i!=source; i=edgeTo[i].getOtherEndNode(i)){ 
				edgeTo[i].increaseFlowTo(i, flowIncrease);
			}
			System.out.println("  flowIncrease="+flowIncrease+" ");
			flow+=flowIncrease;
		}
		System.out.println("\nMaximum pairs matched (maybe maxflow) = "+flow);
	}
	
	//Calls dfs to find an augmentation path & check if it reached the sink
	public boolean existsAugmentingPath(int source, int sink){
		// System.out.println("source="+source+" sink="+sink);

		isVertexMarked = new boolean[vertexCount];		//recreate array of visited nodes each time searching for a path
		isVertexMarked[source] = true;		//visit the source

		quit=false;

		System.out.print("Augmenting Path : S-- ");
		depthFirstSearch(source, sink);		//attempts to find path from source to sink & updates isVertexMarked
		System.out.print("--T  ");

		return isVertexMarked[sink];	//if it reached the sink, then a path was found
	}
	
	boolean quit=false;
	public void depthFirstSearch(int v, int sink){
		// if(v==sink){	//No point in finding a path if the starting vertex is already at the sink/destination
		// 	return;
		// }
		
		if(v==sink)
			quit = true;
		
		for(Edge edge : graph.get(v)){		//loop over all edges in the graph
			if(quit) return;

			int otherEndNode = edge.getOtherEndNode(v);
			boolean isOtherMarked = isVertexMarked[otherEndNode];
			int residCapacity = edge.residualCapacityTo(otherEndNode);
			if(!isVertexMarked[otherEndNode] && edge.residualCapacityTo(otherEndNode)>0 ){	//if otherEndNode is unvisited AND if the residual capacity exists at the otherEndNode
				System.out.print( getStringVertexIdFromArrayIndex.get(otherEndNode) +" ");
				edgeTo[otherEndNode] = edge;		//update next link in edge chain
				isVertexMarked[otherEndNode] = true;		//visit the node
				depthFirstSearch(otherEndNode, sink);		//recursively continue exploring
			}
		}
	}


	public static void main(String[] args){
		int vertexCount = 4;
		int vertexCountIncludingSourceAndSink = vertexCount +2;
		//convert between array indexes (starting from 0) & human readable vertex names
		ArrayList<String> getStringVertexIdFromArrayIndex = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
		getStringVertexIdFromArrayIndex.add("S");	//Add source & sink as last 2 items in the list
		getStringVertexIdFromArrayIndex.add("T");

		int source = vertexCount;	//source & sink as array indexes
		int sink = vertexCount+1;

		CirculationWithDemands circulationGraph = new CirculationWithDemands(vertexCountIncludingSourceAndSink, getStringVertexIdFromArrayIndex);
		circulationGraph.addEdge(source, 0, 3);
		circulationGraph.addEdge(source, 1, 3);
		circulationGraph.addEdge(0, 2, 3);
		circulationGraph.addEdge(0, 3, 1);
		circulationGraph.addEdge(1, 0, 2);
		circulationGraph.addEdge(1, 3, 3);
		circulationGraph.addEdge(2, sink, 2);
		circulationGraph.addEdge(3, 2, 2);
		circulationGraph.addEdge(3, sink, 4);

		circulationGraph.fordFulkersonMaxFlow(source, sink);
		System.out.println("\n");
	}
}