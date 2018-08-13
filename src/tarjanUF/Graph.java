package tarjanUF;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Graph {

    // A HashMap is required to access the GraphNode object given the unique id.
    // This Map is precisely the set of nodes in the graph.
    private final Map<Integer, GraphNode> nodePtrTable;
    // Name associated with the graph.
    private final String name;

    // Constructors:
    public Graph() {
        this.name = null;
        this.nodePtrTable = new HashMap<Integer, GraphNode>();
    }

    public Graph(final String name) {
        this.name = name;
        this.nodePtrTable = new HashMap<Integer, GraphNode>();
    }

    // getName returns None if no name is assigned to graph
    // else returns the name.
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    // get returns a reference to node with node.id == id.
    public GraphNode get(final int id) {
        return this.nodePtrTable.get(id);
    }

    // hasNode checks if a node with node.id == id is present in graph or not.
    public boolean hasNode(final int id) {
        return this.nodePtrTable.containsKey(id);
    }

    // addArc adds an edge from nodeId to arcId.
    // But requires nodeId to be present in the graph.
    public void addArc(int nodeId, int arcId) {
        assert this.nodePtrTable.containsKey(nodeId);
        GraphNode graphNode = this.nodePtrTable.get(nodeId);
        graphNode.getArcs().add(arcId);
    }

    // addNode puts the new node in to the graph.
    public void addNode(GraphNode node) {
        assert !this.nodePtrTable.containsKey(node.getId());
        this.nodePtrTable.put(node.getId(), node);
    }

    // N returns the number of nodes currently present in the graph.
    public Integer N() {
        return nodePtrTable.size();
    }

}
