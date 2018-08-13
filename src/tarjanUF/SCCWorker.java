package tarjanUF;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import tarjanUF.UF.PickStatus;
import tarjanUF.UF.ClaimStatus;

public class SCCWorker implements Runnable {

    private final Graph graph;
    private final Map<Long, Integer> workerMap;
    private final AtomicInteger workerCount;
    // workerId will be assigned in the `run` method since
    // the worker executing can be known only from there.
    private int workerId;
    // nodeId denotes the node from where the DFS is to be started.
    private int nodeId;
    private UF unionfind;
    // Used to eliminate recursion.
    private Stack<Integer> recursionStack;
    // The so called tarjanStack in Tarjan's sequential algorithm.
    private Stack<Integer> rootStack;

    public SCCWorker(final Graph graph,
                     final Map<Long, Integer> workerMap,
                     final AtomicInteger workerCount,
                     final int nodeId,
                     UF unionfind) {
        this.graph = graph;
        this.workerMap = workerMap;
        this.workerCount = workerCount;
        this.nodeId = nodeId;
        this.unionfind = unionfind;
        this.recursionStack = new Stack<Integer>();
        this.rootStack = new Stack<Integer>();
    }

    @Override
    public void run() {
        // Assign a unique integer to this worker or if already assigned set workerId to that.
        if (workerMap.containsKey(Thread.currentThread().getId())) {
            this.workerId = workerMap.get(Thread.currentThread().getId());
        } else {
            this.workerId = workerCount.incrementAndGet();
            workerMap.put(Thread.currentThread().getId(), this.workerId);
        }
        // Let the worker make claim on the intial node so that it can start exploring.
        unionfind.makeClaim(nodeId + 1, workerId);

        PickStatus picked;
        // `ei` is the index from where we should start exploring the arcs of the node `vp`.
        int v, vp, w, ei, root;
        int random_ei;
        ClaimStatus claimed;
        // Used to simulate return from a function.
        boolean backtrack = false;

        v = this.nodeId;

        List<Integer> arcs = null;
        START: while (true) {
            // This indicates a new DFS call in the recursion.
            if (!backtrack) {
                rootStack.push(v);
            }

            LOOP: while (true) {
                if (!backtrack) {
                    // Some other worker might have united the new root and old root.
                    // Exploit that!
                    if (!recursionStack.empty() && unionfind.sameSet(recursionStack.peek() + 1, v + 1)) {
                        break;
                    }

                    // Try to obtain a listLive element in the list of v.
                    Pair<PickStatus, Integer> p = unionfind.pickFromList(v + 1);
                    picked = p.getKey();
                    // If list of `v` is dead than we have discovered the SCC of v and can break.
                    if (picked != PickStatus.pickSuccess) {
                        break;
                    }
                    // The listLive element.
                    vp = p.getValue() - 1;
                    // Initially start from starting of list.
                    ei = 0;
                } else {
                    // Restore the recursion state when backtracking.
                    v = recursionStack.pop();
                    ei = recursionStack.pop() + 1;
                    vp = recursionStack.pop();
                    // Do not backtrack again. We might have to explore further.
                    backtrack = false;
                    // Some other worker can make the node `v` dead.
                    // In this case we simply remove `v` and stop exploring it.
                    if (unionfind.isDead(v + 1)) {
                        unionfind.removeFromList(vp + 1);
                        continue LOOP;
                    }
                }

                arcs = graph.get(vp).getArcs();
                for (; ei < arcs.size(); ei++) {
                    // Randomized the exploration of node `vp` for different workers.
                    random_ei = (ei + workerId) % arcs.size();
                    w = arcs.get(random_ei);
                    // Self loop.
                    if (w == vp) {
                        continue;
                    }
                    // Else let worker obatin a claim on `w`.
                    claimed = unionfind.makeClaim(w + 1, workerId);

                    // If the node `w` is dead we should not explore it.
                    if (claimed == ClaimStatus.claimDead) {
                        continue;
                    } else if (claimed == ClaimStatus.claimSuccess) {
                        // We found a new node. Explore it!
                        // Push the caller-saved values into the stack and
                        // continue with new root `w`.
                        recursionStack.push(vp);
                        recursionStack.push(ei);
                        recursionStack.push(v);
                        v = w;
                        continue START;
                    } else {
                        // We received a claimFound meaning that `w` is already present
                        // in the tarjanStack of `v`. This implies that we found a cycle.
                        // Exploit it!!!
                        while (!unionfind.sameSet(w + 1, v + 1)) {
                            root = rootStack.pop();
                            unionfind.unite(rootStack.peek() + 1, root + 1);
                        }
                    }
                }

                // Done exploring `vp` here and hence can be removed.
                unionfind.removeFromList(vp + 1);
            }

            // Need to avoid exploring `v` again.
            if (rootStack.peek() == v) {
                rootStack.pop();
            }
            // If still we have to backtrack then backtrack.
            // Else we are done exploring the graph from `nodeId`.
            if (!recursionStack.empty()) {
                backtrack = true;
            } else {
                break;
            }
        }
    }

}
