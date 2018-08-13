package tarjanUF;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("serial")
public class GraphNode implements Serializable {

    // List of adjacent nodes that be reached via an outgoing edge from this node.
    private List<Integer> arcs;
    // Each node is associated with a unique id.
    private int id;

    // Constructor.
    public GraphNode(int id) {
        this.id = id;
        // Create a dynamically resizable adjacency list.
        arcs = new ArrayList<Integer>();
    }

    public void setArcs(List<Integer> arcs) {
        this.arcs = arcs;
    }

    public List<Integer> getArcs() {
        return this.arcs;
    }

    // Check if it is a terminal node.
    public boolean hasArcs() {
        if (arcs == null) {
            return false;
        }
        return !this.arcs.isEmpty();
    }

    // Get the unique id associated with this node.
    public int getId() {
        return this.id;
    }

    // POST returns an iterator over the adjacent nodes of this node.
    public Iterator<Integer> POST() {
        assert this.arcs != null;
        return this.arcs.iterator();
    }

}
