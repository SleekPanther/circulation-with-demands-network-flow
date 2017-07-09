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
	public int other(int vertex){
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
	private final int V;
	private int E;
	private ArrayList<ArrayList<FlowEdge>> graph;
	
	FlowNetwork(int V){
		this.V = V;
		graph = new ArrayList<>(V);
		for(int i=0;i<V;++i)graph.add(new ArrayList<>());
	}
	void addEdge(FlowEdge e){
		int v = e.from();
		int w = e.to();
		graph.get(v).add(e);
		graph.get(w).add(e);
		E++;
	}
	
	
	Iterable<FlowEdge> adj(int v){
		return graph.get(v);
	}
	
	Iterable<FlowEdge> edges(){
		ArrayList<FlowEdge> al = new ArrayList<>(V);
		for(int i=0;i<V;++i){
			for(FlowEdge fe:graph.get(i))al.add(fe);
		}
		return al;        
	}
	
	int V(){return V;};
	int E(){return E;};
}

public class FordFulkerson {
	private boolean[] marked;
	private FlowEdge[] edgeTo;
	private double value;

	public FordFulkerson(FlowNetwork G, int s, int t){
		value = 0;
		while(hasAugmentingPath(G, s, t)){ 
			double bottle = Double.POSITIVE_INFINITY;
			System.out.print("Considering Augmenting Path: "+ Arrays.toString(edgeTo));
			 
			for(int v = t;v != s;v=edgeTo[v].other(v)){ 
				bottle = Math.min(bottle, edgeTo[v].residualCapacityTo(v));
			}
			for(int v = t;v!=s;v = edgeTo[v].other(v))
				edgeTo[v].addResidualFlowTo(v, bottle);
			
			System.out.println("\t\tBottleneck="+bottle);
			value += bottle;
		}
	}
	
	//BFS?
	public final boolean hasAugmentingPath(FlowNetwork G, int s, int t){
		edgeTo = new FlowEdge[G.V()];
		marked = new boolean[G.V()];
		
		Queue<Integer> q = new LinkedList<>();
		q.add(s);
		marked[s] = true;
		while(!q.isEmpty()){
			
			int v = q.poll(); 
			for(FlowEdge e:G.adj(v)){
				int w = e.other(v);
				if(e.residualCapacityTo(w) > 0 && !marked[w]){
					edgeTo[w] = e;
					
					marked[w] = true;
					q.add(w);
				}
			}
		}
		return marked[t];
	}
	
	public double value(){
		return value;
	}
	
	public boolean inCut(int v){
		return marked[v];
	} 
	
	public static void main(String[] args){
		// FlowNetwork network = new FlowNetwork(4);
		// network.addEdge(new FlowEdge(0, 1, 20));
		// network.addEdge(new FlowEdge(0, 2, 10));
		// network.addEdge(new FlowEdge(1, 2, 30));
		// network.addEdge(new FlowEdge(1, 3, 10));
		// network.addEdge(new FlowEdge(2, 3, 20));
		// FordFulkerson ff = new FordFulkerson(network, 0, 3);

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
		// FordFulkerson ff = new FordFulkerson(network, 0, 5);

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
		FordFulkerson ff = new FordFulkerson(network, 4, 5);

		System.out.println("Maxflow value = "+ff.value());
		
		System.out.println("Mincut vertices : ");
		for(int i=0;i<network.V();++i){
			if(ff.marked[i]){
				System.out.print(i+" ");
			}
		}
	}
}