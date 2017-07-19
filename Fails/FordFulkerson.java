import java.util.*;
import java.util.Map.Entry;

/**
 * A duplex edge is two edges(forward and reverse edge) combined into one.
 * In ford-fulkerson algo, we have a concept of forward edge and a reverse edge.
 * 
 * The forward edge is when the "from" node equals "from" node set by constructor
 * and 
 * "to" node equals "to" node set by constructor.
 * 
 * Duplex emulates both these edges by returning different values based on "to" and "from"
 * ie, it behaves as both "forward edge" and "backward egde" based on its input parameters.
 *
 * @param <T>
 */
final class DuplexEdge<T> {
	private final T from;
	private final T to;
	private final double capacity;
	private double consumedCapacity;

	public DuplexEdge (T from, T to, double capacity, double consumedCapacity) {
		if (from == null  || to == null) {
			throw new NullPointerException("Neither from nor to should be null.");
		}

		this.from = from;
		this.to = to;
		this.capacity = capacity;
		this.consumedCapacity = consumedCapacity;
	}


	/**
	 * Returns the remaining capacity of that pipe/edge/channel.
	 * From `from` and `to` a determination is made if its a forward edge of backward edge.
	 * Depending on edge type the capacity is returned.
	 * 
	 * @param from      the from/source node   
	 * @param to        the to node.
	 * @return          the remaining capacity on determing if its a forward or reverse edge.
	 */
	public double getCapacity(T from, T  to) {
		if (this.from.equals(from) && this.to.equals(to)) {
			 return capacity - consumedCapacity;
		} 

		// indicates reverse flow.
		if (this.from.equals(to) && this.to.equals(from)) {
		   return consumedCapacity;
		}
		throw new IllegalArgumentException("Both from: " + from + " and to : " + to + " should be part of this edge.");
	} 


	/**
	 * Adjusts/modifies the remaining capacity of that pipe/edge/channel.
	 * From `from` and `to` a determination is made if its a forward edge of backward edge.
	 * Depending on edge type the capacity is adjusted.
	 * 
	 * @param from      the from/source node   
	 * @param to        the to node.
	 * @return          the remaining capacity on determing if its a forward or reverse edge.
	 */
	public double adjustCapacity(T from, T  to, double consumedCapacity) {
		if (consumedCapacity > getCapacity(from, to)) {
			throw new IllegalArgumentException("The consumedCapacity " + consumedCapacity + " exceeds limit.");
		}

		if (this.from.equals(from) && this.to.equals(to)) {
			this.consumedCapacity = this.consumedCapacity + consumedCapacity;
		}

		// indicates reverse flow.
		if (this.from.equals(to) && this.to.equals(from)) {
			this.consumedCapacity = this.consumedCapacity - consumedCapacity;
		}

		throw new IllegalArgumentException("Both from: " + from + " and to : " + to + " should be part of this edge.");
	}
}

class GraphFordFuklerson<T> implements Iterable<T> {

	/* A map from nodes in the graph to sets of outgoing edges.  Each
	 * set of edges is represented by a map from edges to doubles.
	 */
	private final Map<T, Map<T, DuplexEdge<T>>> graph;

	public GraphFordFuklerson() {
		graph = new HashMap<T, Map<T, DuplexEdge<T>>>();
	}

	/**
	 *  Adds a new node to the graph. If the node already exists then its a
	 *  no-op.
	 * 
	 * @param node  Adds to a graph. If node is null then this is a no-op.
	 * @return      true if node is added, false otherwise.
	 */
	public boolean addNode(T node) {
		if (node == null) {
			throw new NullPointerException("The input node cannot be null.");
		}
		if (graph.containsKey(node)) return false;

		graph.put(node, new HashMap<T, DuplexEdge<T>>());
		return true;
	}

	/**
	 * Given the source and destination node it would add an arc from source 
	 * to destination node. If an arc already exists then the value would be 
	 * updated the new value.
	 *  
	 * @param source                    the source node.
	 * @param destination               the destination node.
	 * @param capacity                    if length if 
	 * @throws NullPointerException     if source or destination is null.
	 * @throws NoSuchElementException   if either source of destination does not exists. 
	 */
	public void addEdge (T source, T destination, double capacity) {
		if (source == null || destination == null) {
			throw new NullPointerException("Source and Destination, both should be non-null.");
		}
		if (!graph.containsKey(source) || !graph.containsKey(destination)) {
			throw new NoSuchElementException("Source and Destination, both should be part of graph");
		}
		DuplexEdge<T> duplexEdge = new DuplexEdge<T>(source, destination, capacity, 0);

		/* A node would always be added so no point returning true or false */
		graph.get(source).put(destination, duplexEdge);
		graph.get(destination).put(source, duplexEdge);
	}

	/**
	 * Removes an edge from the graph.
	 * 
	 * @param source        If the source node.
	 * @param destination   If the destination node.
	 * @throws NullPointerException     if either source or destination specified is null
	 * @throws NoSuchElementException   if graph does not contain either source or destination
	 */
	public void removeEdge (T source, T destination) {
		if (source == null || destination == null) {
			throw new NullPointerException("Source and Destination, both should be non-null.");
		}
		if (!graph.containsKey(source) || !graph.containsKey(destination)) {
			throw new NoSuchElementException("Source and Destination, both should be part of graph");
		}
		graph.get(source).remove(destination);
		graph.get(destination).remove(source);
	}

	/**
	 * Given a node, returns the edges going outward that node,
	 * as an immutable map.
	 * 
	 * @param node The node whose edges should be queried.
	 * @return An immutable view of the edges leaving that node.
	 * @throws NullPointerException   If input node is null.
	 * @throws NoSuchElementException If node is not in graph.
	 */
	public Map<T, DuplexEdge<T>> edgesFrom(T node) {
		if (node == null) {
			throw new NullPointerException("The node should not be null.");
		}
		Map<T, DuplexEdge<T>> edges = graph.get(node);
		if (edges == null) {
			throw new NoSuchElementException("Source node does not exist.");
		}
		return Collections.unmodifiableMap(edges);
	}

	/**
	 * Returns the iterator that travels the nodes of a graph.
	 * 
	 * @return an iterator that travels the nodes of a graph.
	 */
	@Override
	public Iterator<T> iterator() {
		return graph.keySet().iterator();
	}
}

public final class FordFulkerson<T> {
	private final GraphFordFuklerson<T> graph;


	/**
	 * Takes in a graph, which should not be modified by client.
	 * However client should note that graph object is going to be changed by 
	 * FordFulkerson algorithm.
	 * 
	 * @param graph the input graph.
	 */
	public FordFulkerson (GraphFordFuklerson<T> graph) {
		if (graph == null) {
			throw new NullPointerException("The graph should not be null");
		}
		this.graph = graph;
	}


	private void validate(T source, T destination) {
		if (source == null || destination == null) {
			throw new NullPointerException("Neither source nor destination should be null");
		}
		if (source.equals(destination)) {
			throw new IllegalArgumentException("The source should not be the same as destination.");
		}
	}

	/**
	 * Determines the max flow based on ford-fulkerson algorithm.
	 * 
	 * @param source            the source node.    
	 * @param destination       the destination node
	 * @return                  the max-flow
	 */
	public double maxFlow(T source, T destination) {
		validate(source, destination);
		double max = 0;
		List<T> nodes = getPath(source, destination);
		while (nodes.size() > 0) {
			double maxCapacity = maxCapacity(nodes);
			max = max + maxCapacity;
			drainCapacity(nodes, maxCapacity);
			nodes = getPath(source, destination);
		}
		return max;
	}

	/**
	 * Gets the path from source node to destination node, such that there is 
	 * capacity > 0 at each edge from source to destination.
	 * 
	 * @param source        the source node
	 * @param destination   the destination node
	 * @return              the path from source to destination, 
	 */
	private List<T> getPath(T source, T destination) {
		synchronized (graph) {
			final LinkedHashSet<T> path = new LinkedHashSet<T>();
			depthFind(source, destination, path);
			return new ArrayList<T>(path);
		}
	}

	//recursive depth 1st search?
	private boolean depthFind(T current, T destination, LinkedHashSet<T> path) {
		path.add(current);

		if (current.equals(destination)) {
			return true;
		}
		//Set<Entry<T, DuplexEdge<T>>> a = graph.edgesFrom(current).entrySet();
		for (Entry<T, DuplexEdge<T>> entry : graph.edgesFrom(current).entrySet()) {
			// if not cycle and if capacity exists.
			if (!path.contains(entry.getKey()) && entry.getValue().getCapacity(current, entry.getKey()) > 0) {
				// if end has been reached.
				if (depthFind(entry.getKey(), destination, path)) {
					return true;
				}
			}
		}

		path.remove(current);
		return false;
	}

	/**
	 * Returns the maximum capacity in the path.
	 * Maximum capacity is the minimim capacity available on the path
	 * from source to destination
	 * 
	 * @param nodes     the nodes that contibute a path
	 * @return          the max capacity on the path.
	 */ 
	private double maxCapacity(List<T> nodes) {
		double maxCapacity = Double.MAX_VALUE;
		for (int i = 0; i < nodes.size() - 1; i++) {
			T source = nodes.get(i);
			T destination = nodes.get(i + 1);

			DuplexEdge<T> duplexEdge = graph.edgesFrom(source).get(destination);
			double capacity = duplexEdge.getCapacity(source, destination);
			if (maxCapacity > capacity) { 
				maxCapacity = capacity;
			}
		}
		return maxCapacity;
	}

	/**
	 * Reduces the capacity along the path from source to destination
	 * 
	 * @param nodes           the nodes that contribute the path
	 * @param maxCapacity     the maximum capacity along the path.
	 */ 
	private void drainCapacity (List<T> nodes, double maxCapacity) {
		for (int i = 0; i < nodes.size() - 1; i++) {
			T source = nodes.get(i);
			T destination = nodes.get(i + 1);

			DuplexEdge<T> duplexEdge = graph.edgesFrom(source).get(destination);
			duplexEdge.adjustCapacity(source, destination, maxCapacity);
		}
	}


	public static void main(String[] args) {
		final GraphFordFuklerson<String> graph = new GraphFordFuklerson<String>();
		graph.addNode("S");
		graph.addNode("A");
		graph.addNode("B");
		graph.addNode("T");

		// graph.addNode("A");
		// graph.addNode("B");
		// graph.addNode("C");
		// graph.addNode("D");
		// graph.addNode("E");
		// graph.addNode("F");
		// graph.addNode("G");
		// graph.addNode("H");

		graph.addEdge("S", "A", 20);
		graph.addEdge("S", "B", 10);
		graph.addEdge("A", "T", 10);
		graph.addEdge("A", "B", 30);
		graph.addEdge("B", "T", 20);

		// graph.addEdge("A", "B", 10);
		// graph.addEdge("A", "C", 5);
		// graph.addEdge("A", "D", 15);
		// graph.addEdge("B", "C", 4);
		// graph.addEdge("C", "D", 4);
		// graph.addEdge("B", "E", 9);
		// graph.addEdge("B", "F", 15);
		// graph.addEdge("C", "F", 8);
		// graph.addEdge("D", "G", 16);
		// graph.addEdge("E", "F", 15);
		// graph.addEdge("F", "G", 15);
		// graph.addEdge("G", "C",  6);
		// graph.addEdge("E", "H", 10);
		// graph.addEdge("F", "H", 10);
		// graph.addEdge("G", "H", 10);

		FordFulkerson<String> ff = new FordFulkerson<String>(graph);
		double value = ff.maxFlow("S", "T");
		// double value = ff.maxFlow("A", "H");
		System.out.println("assert 28 = "+value);
		//assertEquals(28.0, value, 0);
	}
}