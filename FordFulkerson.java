import java.util.*;

class FlowEdge {
	private final int fromVertex;
	private final int toVertex;
	private double capacity;
	private double flow;
	private double lowerBound;
	private double upperBound;
	
	public FlowEdge(int fromVertex, int toVertex, double capacity){
		this.fromVertex = fromVertex;
		this.toVertex = toVertex;
		this.capacity = capacity;
	}
	public FlowEdge(int fromVertex, int toVertex, double lowerBound, double upperBound){
		this.fromVertex = fromVertex;
		this.toVertex = toVertex;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
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
	public double getCapacity(){
		return capacity;
	}
	public void setCapacity(double capacity){
		this.capacity=capacity;
	}

	public double flow(){
		return flow;
	}

	public double getLowerBound(){
		return lowerBound;
	}
	public void setLowerBound(double newBound){
		lowerBound=newBound;
	}
	public double getUpperBound(){
		return upperBound;
	}
	public void setUpperBound(double newBound){
		upperBound=newBound;
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
		return "["+fromVertex+"-->"+toVertex+" (capacity="+capacity+")]";
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
	public void addVertexPlaceholder(){			//When a new vertex needs to be appended to the graph
		graph.add(new ArrayList<FlowEdge>());
		vertexCount++;
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
	private double maxFlow = 0;
	private int sumOfDemands = 0;
	private int sumOfSupplies = 0;
	private int lowerBoundsAdjustedsumOfDemands=0;
	private int lowerBoundsAdjustedsumOfSupplies=0;
	private boolean doDemandsMatchSupplies=true;
	private boolean hasCirculation = true;
	private boolean hasLowerBounds = false;

	private boolean[] marked;
	private FlowEdge[] edgeTo;

	public FordFulkerson(FlowNetwork graph, ArrayList<String> vertexName, int[] vertexDemand){
		ArrayList<Integer> demandVertices = new ArrayList<Integer>();
		ArrayList<Integer> supplyVertices = new ArrayList<Integer>();
		for(int vertex=0; vertex<graph.vertexCount(); vertex++){
			if(vertexDemand[vertex]>0){
				demandVertices.add(vertex);
				sumOfDemands += vertexDemand[vertex];
			}
			else if(vertexDemand[vertex]<0){
				supplyVertices.add(vertex);
				sumOfSupplies += -vertexDemand[vertex];		//negative
			}
			//If demand=0 nothing needs to change, vertex is not connected to source or sink
		}
		
		if(sumOfSupplies != sumOfDemands){
			doDemandsMatchSupplies=false;
		}

		if(doDemandsMatchSupplies){		//Only continue if supplies/demands are valid
			//Process edges and adjust for lower bounds
			for(FlowEdge edge : graph.edges()){
				if(edge.getLowerBound() != 0){		//Edges with NO lower bounds have lower bound of 0
					hasLowerBounds=true;
					//Subtract lower bounds from capacity & update bounds
					double oldLowerBound = edge.getLowerBound();
					edge.setCapacity(edge.getUpperBound() - oldLowerBound);		//lower bound edges initally have no capacity
					edge.setUpperBound(edge.getCapacity());
					edge.setLowerBound(0);

					//Adjust supplies/demands for both ends of the edge. Subtract oldLowerBound if vertex is a demand vertex (>0) & add if it's a supply vertex (<0)
					if(vertexDemand[edge.from()]>0){
						vertexDemand[edge.from()] -= oldLowerBound;
					}else{
						vertexDemand[edge.from()] += oldLowerBound;
					}

					if(vertexDemand[edge.to()]>0){
						vertexDemand[edge.to()] -= oldLowerBound;
					}else{
						vertexDemand[edge.to()] += oldLowerBound;
					}
				}
			}

			//Recalculate Sum of supplies/demands with adjusted bounds
			if(hasLowerBounds){
				lowerBoundsAdjustedsumOfDemands=0;
				lowerBoundsAdjustedsumOfSupplies=0;
				for(int vertex=0; vertex<graph.vertexCount(); vertex++){
					if(vertexDemand[vertex]>0){
						lowerBoundsAdjustedsumOfDemands += vertexDemand[vertex];
					}
					else if(vertexDemand[vertex]<0){
						lowerBoundsAdjustedsumOfSupplies += -vertexDemand[vertex];		//negative
					}
					//If demand=0 nothing needs to change, vertex is not connected to source or sink
				}
				if(lowerBoundsAdjustedsumOfSupplies != lowerBoundsAdjustedsumOfDemands){
					doDemandsMatchSupplies=false;
				}
			}

			if(doDemandsMatchSupplies){
				//Add S & T, connect to supply/demand vertices
				int source = graph.vertexCount();
				int sink = source + 1;

				vertexName.add("S");
				vertexName.add("T");

				graph.addVertexPlaceholder();
				graph.addVertexPlaceholder();

				//Connect demand vertices to sink & source vertex to all supply vertices
				for(int vertex : demandVertices){
					graph.addEdge(new FlowEdge(vertex, sink, vertexDemand[vertex]));
				}
				for(int vertex : supplyVertices){
					graph.addEdge(new FlowEdge(source, vertex, -vertexDemand[vertex]));		//negative of the demand value to get a positive capacity 
				}


				//Begin Ford Fulkerson part
				maxFlow = 0;

				while(hasAugmentingPath(graph, source, sink)){ 
					double bottneckFlow = Double.POSITIVE_INFINITY;
					
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

					System.out.print("Bottleneck Flow="+bottneckFlow);
					System.out.print("\tAugmenting Path: ");
					System.out.print(vertexName.get(source));
					for(int i=augmentingPathBackwards.size()-1; i>=0; i--){
						System.out.print("-->"+vertexName.get(augmentingPathBackwards.get(i)));
					}
					System.out.println();
					
					maxFlow += bottneckFlow;
				}
			}
		}
		displayOutputMessages(graph, vertexName);
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
	public int sumOfDemands(){
		return sumOfDemands;
	}
	public int sumOfSupplies(){
		return sumOfSupplies;
	}
	public boolean doDemandsMatchSupplies(){
		return doDemandsMatchSupplies;
	}
	public boolean hasCirculation(){
		if(!doDemandsMatchSupplies){
			return false;
		}
		else if(hasLowerBounds){
			if(maxFlow!=lowerBoundsAdjustedsumOfSupplies || maxFlow!=lowerBoundsAdjustedsumOfDemands){
				return false;
			}
		}
		else if(maxFlow!=sumOfSupplies || maxFlow!=sumOfDemands){
			return false;
		}
		return true;
	}

	private void displayOutputMessages(FlowNetwork graph, ArrayList<String> vertexName){
		if(hasCirculation()){
			System.out.println("Graph has Circulation \nMaxflow value = "+maxFlow);
			System.out.println("\nMincut vertices: ");
			for(int v=0; v<graph.vertexCount(); ++v){
				if(marked[v]){
					System.out.print(vertexName.get(v)+" ");
				}
			}
		}else{
			System.out.println("Graph does NOT have circulation");
			if(!doDemandsMatchSupplies()){
				System.out.println("Demands & supplies do not match");
				System.out.println("Sum of demands = "+sumOfDemands());
				System.out.println("Sum of supplies = "+sumOfSupplies());
			}
			else{
				System.out.println("Maxflow="+maxFlow +"  should match sum of demands & supplies \nOnly the source node \"S\" should be in the mincut");
			}
		}
	}
	
	public boolean isVertexinCut(int vertex){
		return marked[vertex];
	}


	public static void main(String[] args){
		// // Ford fulkerson graph, not a circulation graph
		// FlowNetwork network = new FlowNetwork(6);
		// network.addEdge(new FlowEdge(4, 0, 16));
		// network.addEdge(new FlowEdge(4, 1, 13));
		// network.addEdge(new FlowEdge(0, 2, 12));
		// network.addEdge(new FlowEdge(1, 0, 4));
		// network.addEdge(new FlowEdge(1, 3, 14));
		// network.addEdge(new FlowEdge(2, 5, 20));
		// network.addEdge(new FlowEdge(2, 1, 9));
		// network.addEdge(new FlowEdge(3, 2, 7));
		// network.addEdge(new FlowEdge(3, 5, 4));
		// ArrayList<String> vertexName = new ArrayList<String>(Arrays.asList("v1", "v2", "v3", "v4"));
		// FordFulkerson fordFulkerson = new FordFulkerson(network, vertexName, 4, 5);


		//Simple Circulation graph
		// FlowNetwork network = new FlowNetwork(4);
		// network.addEdge(new FlowEdge(0, 2, 3));
		// network.addEdge(new FlowEdge(0, 3, 1));
		// network.addEdge(new FlowEdge(1, 0, 2));
		// network.addEdge(new FlowEdge(1, 3, 3));
		// network.addEdge(new FlowEdge(3, 2, 2));
		// ArrayList<String> vertexName = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
		// int[] vertexDemand = {-3, -3, 2, 4};
		// FordFulkerson fordFulkerson = new FordFulkerson(network, vertexName, vertexDemand);

		//Complex circulation graph
		// FlowNetwork network = new FlowNetwork(6);
		// network.addEdge(new FlowEdge(0, 3, 6));	//a d
		// network.addEdge(new FlowEdge(0, 4, 7));	//a e
		// network.addEdge(new FlowEdge(1, 3, 7));	//b d
		// network.addEdge(new FlowEdge(1, 5, 9));	//b f
		// network.addEdge(new FlowEdge(2, 0, 10));//c a
		// network.addEdge(new FlowEdge(2, 3, 3));	//c d
		// network.addEdge(new FlowEdge(4, 1, 4));	//e b
		// network.addEdge(new FlowEdge(4, 5, 4));	//e f
		// ArrayList<String> vertexName = new ArrayList<String>(Arrays.asList("a", "b", "c", "d", "e", "f"));
		// int[] vertexDemand = {-8, -6, -7, 10, 0, 11};
		// FordFulkerson fordFulkerson = new FordFulkerson(network, vertexName, vertexDemand);

		//Graph 5 with no lower bounds
		// FlowNetwork network = new FlowNetwork(4);
		// network.addEdge(new FlowEdge(0, 2, 4));
		// network.addEdge(new FlowEdge(0, 1, 5));
		// network.addEdge(new FlowEdge(1, 2, 5));
		// network.addEdge(new FlowEdge(1, 3, 4));
		// network.addEdge(new FlowEdge(2, 3, 3));
		// ArrayList<String> vertexName = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
		// int[] vertexDemand = {-3, -4, 2, 5};
		// FordFulkerson fordFulkerson = new FordFulkerson(network, vertexName, vertexDemand);

		//Graph 5
		FlowNetwork network = new FlowNetwork(4);
		network.addEdge(new FlowEdge(0, 2, 4));
		network.addEdge(new FlowEdge(0, 1, 5));
		network.addEdge(new FlowEdge(1, 2, 1, 5));	//has lower & upper bound
		network.addEdge(new FlowEdge(1, 3, 4));
		network.addEdge(new FlowEdge(2, 3, 3));
		ArrayList<String> vertexName = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
		int[] vertexDemand = {-3, -4, 2, 5};
		FordFulkerson fordFulkerson = new FordFulkerson(network, vertexName, vertexDemand);
	}
}