package tarjanUF;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    // readFile reads a directed graph from the file `filename` which contains an edge in each line.
    // The graph is stored as an adjacency list.
    private static void readFile(Graph graph, String filename) throws IOException {
        final long start = System.nanoTime();

        final FileInputStream in = new FileInputStream(filename);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                final String[] split = line.trim().split("\\s+");
                final int nodeId = Integer.parseInt(split[0]);
                final int arcId = Integer.parseInt(split[1]);
                if (graph.hasNode(nodeId)) {
                    // If the graph already has `nodeId`, simply append in `nodeId`s list.
                    graph.addArc(nodeId, arcId);
                } else {
                    // Else create a new node for `nodeId`.
                    graph.addNode(new GraphNode(nodeId));
                    // Then add the edge.
                    graph.addArc(nodeId, arcId);
                }

                // Make sure that a node `arcId` is present
                // as `arcId` might not have an outgoing edge.
                if (!graph.hasNode(arcId)) {
                    graph.addNode(new GraphNode(arcId));
                }
            }
        }

        final long duration = System.nanoTime() - start;
        System.err.println("Runtime for input: " + duration);
    }

    // readInits reads the set of initial nodes from which the entire graph can be discovered.
    // We start graph traversal from this nodes only.
    public static void readInits(List<Integer> initNodes, String filename) throws IOException {
        final FileInputStream in = new FileInputStream(filename);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                line = line.trim();
                initNodes.add(Integer.parseInt(line));
            }
        }
    }

    // printSCCs outputs the SCCs found with each SCC in a newline.
    public static void printSCCs(Map<Integer, Set<GraphNode>> sccs) {
        final long start = System.nanoTime();

        for (Set<GraphNode> sgn: sccs.values()) {
            for (GraphNode gn: sgn) {
                System.out.print(gn.getId());
                System.out.print(" ");
            }
            System.out.println();
        }

        final long duration = System.nanoTime() - start;
        System.err.println("Runtime for output: " + duration);
    }

    public static void main(String[] args) {
        // The program requires 3 parameters in its input.
        // 1. The graph to be processed.
        // 2. The set of intital nodes from which entire graph can be discoverd.
        // 3. Number of threads on which algorithm needs to run. #threads = -1 implies maximum available threads.
        assert args.length == 3;
        System.err.println("Processing graph: " + args[0] + " starting with initial nodes from " + args[2] + " with " + args[1] + " threads.");
        System.err.println("Runtimes are in nanoseconds.");
        final long start = System.nanoTime();

        // Read inputs from file.
        final Graph graph = new Graph(args[0]);
        final List<Integer> initNodes = new ArrayList<Integer>();
        try {
            readFile(graph, args[0]);
            readInits(initNodes, args[2]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get the number of threads on which we should run the algorithm.
        UFNode.workerCount = ConcurrentFastSCC.requiredProcessors(Integer.parseInt(args[1]));
        // Create a new union find datastructure to maintain SCCs.
        final UF unionfind = new UF(graph.N() + 1);
        // Run the aglorithm.
        final Map<Integer, Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph, initNodes, unionfind, UFNode.workerCount);

        printSCCs(sccs);

        final long duration = System.nanoTime() - start;
        System.err.println("Total runtime: " + duration);
    }
}
