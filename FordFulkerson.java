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

	public FordFulkerson(FlowNetwork graph, int source, int sink){
		maxFlow = 0;
		while(hasAugmentingPath(graph, source, sink)){ 
			double bottneckFlow = Double.POSITIVE_INFINITY;
			System.out.print("Considering Augmenting Path: "+ Arrays.toString(edgeTo));
			
			//Loop over path & find bottleneck
//Here is where to print the actual path (hopefully)
			for(int v = sink; v != source; v=edgeTo[v].otherVertex(v)){ 
				bottneckFlow = Math.min(bottneckFlow, edgeTo[v].residualCapacityTo(v));
			}
			//Update residual Capacities
			for(int v = sink; v!=source; v = edgeTo[v].otherVertex(v)){
				edgeTo[v].addResidualFlowTo(v, bottneckFlow);
			}
			
			System.out.println("\t\tBottleneck="+bottneckFlow);
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
		// FlowNetwork network = new FlowNetwork(4);
		// network.addEdge(new FlowEdge(0, 1, 20));
		// network.addEdge(new FlowEdge(0, 2, 10));
		// network.addEdge(new FlowEdge(1, 2, 30));
		// network.addEdge(new FlowEdge(1, 3, 10));
		// network.addEdge(new FlowEdge(2, 3, 20));
		// FordFulkerson fordFulkerson = new FordFulkerson(network, 0, 3);

		// FlowNetwork network = new FlowNetwork(6);
		// network.addEdge(new FlowEdge(0, 1, 16));
		// network.addEdge(new FlowEdge(0,2,13));
		// network.addEdge(new FlowEdge(2,1,4));
		// network.addEdge(new FlowEdge(1,3,12));
		// network.addEdge(new FlowEdge(3,2,9));
		// network.addEdge(new FlowEdge(2,4,14));
		// network.addEdge(new FlowEdge(4,3,7));
		// network.addEdge(new FlowEdge(3,5,20));
		// network.addEdge(new FlowEdge(4,5,4));
		// FordFulkerson fordFulkerson = new FordFulkerson(network, 0, 5);

		//Circulation graph
		FlowNetwork network = new FlowNetwork(6);
		network.addEdge(new FlowEdge(4, 0, 3));
		network.addEdge(new FlowEdge(4, 1, 3));
		network.addEdge(new FlowEdge(0, 2, 3));
		network.addEdge(new FlowEdge(0, 3, 1));
		network.addEdge(new FlowEdge(1, 0, 2));
		network.addEdge(new FlowEdge(1, 3, 3));
		network.addEdge(new FlowEdge(2, 5, 2));
		network.addEdge(new FlowEdge(3, 2, 2));
		network.addEdge(new FlowEdge(3, 5, 4));
		FordFulkerson fordFulkerson = new FordFulkerson(network, 4, 5);

		System.out.println("Maxflow value = "+fordFulkerson.maxFlow());
		
		System.out.println("Mincut vertices : ");
		for(int i=0; i<network.vertexCount(); ++i){
			if(fordFulkerson.marked[i]){
				System.out.print(i+" ");
			}
		}
	}
}