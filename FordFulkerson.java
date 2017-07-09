import java.util.*;

class FlowEdge {
	private final int fromVertex;
	private final int toVertex;
	private double capacity;
	private double flow;
	
	public FlowEdge(int fromVertex, int toVertex, double capacity){
		this.fromVertex = fromVertex;
		this.toVertex = toVertex;
		this.capacity = capacity;
	}
	
	public int from(){
		return fromVertex;
	}
	public int to(){
		return toVertex;
	}
	public int otherVertex(int vertex){
		if(vertex==this.fromVertex){
			return toVertex;
		}else{
			return fromVertex;
		}
	}
	public double capacity(){
		return capacity;
	}
	public double flow(){
		return flow;
	}

	public double residualCapacityTo(int vertex){
		if(vertex==toVertex){
			return capacity-flow;
		}
		else return flow;
	}

	public void addResidualFlowTo(int vertex, double changeInFlow){
		if(vertex==this.toVertex){
			flow+=changeInFlow;
		}else{
			flow-=changeInFlow;
		}
	}
	
	@Override
	public String toString(){
		return "["+fromVertex+"-->"+toVertex+" ("+capacity+")]";
	}
}


class FlowNetwork {
	private int vertexCount;
	private int edgeCount;
	private ArrayList<ArrayList<FlowEdge>> graph;
	
	public FlowNetwork(int vertexCount){
		this.vertexCount = vertexCount;
		graph = new ArrayList<ArrayList<FlowEdge>>(vertexCount);
		for(int i=0; i<vertexCount; ++i){
			graph.add(new ArrayList<FlowEdge>());
		}
	}
	public void addEdge(FlowEdge edge){
		int v = edge.from();
		int w = edge.to();
		graph.get(v).add(edge);
		graph.get(w).add(edge);
		edgeCount++;
	}
	

	public int vertexCount(){
		return vertexCount;
	}
	public int edgeCount(){
		return edgeCount;
	}
	
	public Iterable<FlowEdge> adjacentTo(int vertex){
		return graph.get(vertex);
	}
	
	public Iterable<FlowEdge> edges(){
		ArrayList<FlowEdge> edges = new ArrayList<FlowEdge>(vertexCount);
		for(int i=0; i<vertexCount; ++i){
			for(FlowEdge edge:graph.get(i)){
				edges.add(edge);
			}
		}
		return edges;
	}
}


public class FordFulkerson {
	private double maxFlow;

	private boolean[] marked;
	private FlowEdge[] edgeTo;

	public FordFulkerson(FlowNetwork graph, ArrayList<String> vertexName, int source, int sink){
		vertexName.add("S");
		vertexName.add("T");
		//do more preprocessing to connect sink/source to supplies/demands
		
		maxFlow = 0;

		while(hasAugmentingPath(graph, source, sink)){ 
			double bottneckFlow = Double.POSITIVE_INFINITY;
			// System.out.print("Considering Augmenting Path: "+ Arrays.toString(edgeTo));
			
			//Loop backwards over path & find the bottleneck flow
			ArrayList<Integer> augmentingPathBackwards = new ArrayList<Integer>();		//save vertices on the path while looping backwards
			for(int v = sink; v!=source; v=edgeTo[v].otherVertex(v)){
				augmentingPathBackwards.add(v);
				bottneckFlow = Math.min(bottneckFlow, edgeTo[v].residualCapacityTo(v));
			}
			//Update residual Capacities
			for(int v = sink; v!=source; v=edgeTo[v].otherVertex(v)){
				edgeTo[v].addResidualFlowTo(v, bottneckFlow);
			}

			System.out.print("Augmenting Path: ");
			System.out.print(vertexName.get(source));
			for(int i=augmentingPathBackwards.size()-1; i>=0; i--){
				System.out.print("-->"+vertexName.get(augmentingPathBackwards.get(i)));
			}
			
			System.out.println("\t\t\tBottleneck Flow="+bottneckFlow);
			maxFlow += bottneckFlow;
		}
	}
	
	//Breadth first search
	public boolean hasAugmentingPath(FlowNetwork graph, int source, int sink){
		edgeTo = new FlowEdge[graph.vertexCount()];
		marked = new boolean[graph.vertexCount()];
		
		Queue<Integer> vertexQueue = new LinkedList<Integer>();
		vertexQueue.add(source);	//add & visit the source vertex
		marked[source] = true;
		while(!vertexQueue.isEmpty()){
			int vertex = vertexQueue.poll();		//remove vertex from head of queue
			for(FlowEdge edge : graph.adjacentTo(vertex)){
				int otherVertex = edge.otherVertex(vertex);
				if(edge.residualCapacityTo(otherVertex)>0 && !marked[otherVertex]){		//if vertex has residual capacity & is unvisited
					edgeTo[otherVertex] = edge;		//update the edges leading out of otherVertex
					
					marked[otherVertex] = true;		//visit the new vertex
					vertexQueue.add(otherVertex);	//and add to queue
				}
			}
		}
		return marked[sink];	//did BFS visit the target
	}
	
	public double maxFlow(){
		return maxFlow;
	}
	
	public boolean isVertexinCut(int vertex){
		return marked[vertex];
	}


	public static void main(String[] args){
		FlowNetwork network = new FlowNetwork(6);
		network.addEdge(new FlowEdge(4, 0, 16));
		network.addEdge(new FlowEdge(4, 1, 13));
		network.addEdge(new FlowEdge(0, 2, 12));
		network.addEdge(new FlowEdge(1, 0, 4));
		network.addEdge(new FlowEdge(1, 3, 14));
		network.addEdge(new FlowEdge(2, 5, 20));
		network.addEdge(new FlowEdge(2, 1, 9));
		network.addEdge(new FlowEdge(3, 2, 7));
		network.addEdge(new FlowEdge(3, 5, 4));
		ArrayList<String> vertexName = new ArrayList<String>(Arrays.asList("v1", "v2", "v3", "v4"));
		FordFulkerson fordFulkerson = new FordFulkerson(network, vertexName, 4, 5);

		// //Circulation graph
		// FlowNetwork network = new FlowNetwork(6);
		// network.addEdge(new FlowEdge(4, 0, 3));
		// network.addEdge(new FlowEdge(4, 1, 3));
		// network.addEdge(new FlowEdge(0, 2, 3));
		// network.addEdge(new FlowEdge(0, 3, 1));
		// network.addEdge(new FlowEdge(1, 0, 2));
		// network.addEdge(new FlowEdge(1, 3, 3));
		// network.addEdge(new FlowEdge(2, 5, 2));
		// network.addEdge(new FlowEdge(3, 2, 2));
		// network.addEdge(new FlowEdge(3, 5, 4));
		// ArrayList<String> vertexName = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
		// FordFulkerson fordFulkerson = new FordFulkerson(network, vertexName, 4, 5);

		System.out.println("Maxflow value = "+fordFulkerson.maxFlow());
		
		System.out.println("Mincut vertices: ");
		for(int v=0; v<network.vertexCount(); ++v){
			if(fordFulkerson.marked[v]){
				System.out.print(vertexName.get(v)+" ");
			}
		}
	}
}