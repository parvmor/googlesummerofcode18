package tarjanUF;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConcurrentFastSCC {

    // requiredProcessors returns the number of processors to be used in the algorithm.
    // If threads == -1 then maximum number of available processors is returned.
    public static int requiredProcessors(final int threads) {
        final int availableProcessors;
        if (threads == -1) {
            availableProcessors = Runtime.getRuntime().availableProcessors();
        } else {
            availableProcessors = threads;
        }
        System.err.println("Using " + availableProcessors + " processesors.");
        return availableProcessors;
    }

    // searchSCCs initiates `numCores` instances of the SCC algorithm.
    public Map<Integer, Set<GraphNode>> searchSCCs(final Graph graph, final List<Integer> initNodes, final UF unionfind, final int numCores) {
        final ExecutorService executor = Executors.newFixedThreadPool(numCores);
        // workers are not necessarily assigned integer consecutive ids. This is used to map them
        // to [0..`numCores` - 1]
        final Map<Long, Integer> workerMap = new ConcurrentHashMap<Long, Integer>();
        // workerCount is the counter for workerMap.
        final AtomicInteger workerCount = new AtomicInteger(0);

        final long start = System.nanoTime();

        // Initilly queue each node in `initNodes` for a DFS traversal.
        for (int i = 0; i < initNodes.size(); i++) {
            int nodeId = initNodes.get(i);
            if (unionfind.visited.get(nodeId) == false) {
                executor.execute(new SCCWorker(graph, workerMap, workerCount, nodeId, unionfind));
            }
        }
        // If still some threads are ideal, queue the same nodes again.
        // This is not redundant as a node is explored in a randomized fashion
        // by a thread. More specifically, a thread with id `a` will explore with `a` offset.
        if (initNodes.size() < numCores) {
            int leftCores = numCores - initNodes.size();
            for (int i = 0; i < leftCores; i++) {
                int nodeId = initNodes.get(i % initNodes.size());
                if (unionfind.visited.get(nodeId) == false) {
                    executor.execute(new SCCWorker(graph, workerMap, workerCount, nodeId, unionfind));
                }
            }
        }
        // Block the executor's queue from further queueing.
        executor.shutdown();
        try {
            // Await the termination of all threads.
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final long duration = System.nanoTime() - start;
        System.err.println("Runtime for algorithm: " + duration);

        // Organise the SCCs found in a Map of Sets. Map is required to keep track
        // of roots of a union find tree.
        final Map<Integer, Set<GraphNode>> result = new HashMap<Integer, Set<GraphNode>>();
        for (int i = 0; i < graph.N(); i++) {
            int root = unionfind.find(i + 1) - 1;
            if (!result.containsKey(root)) {
                result.put(root, new HashSet<GraphNode>());
            }
            result.get(root).add(graph.get(i));
        }
        return result;
    }

}
