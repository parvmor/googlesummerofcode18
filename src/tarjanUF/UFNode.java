package tarjanUF;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class UFNode {

    // workerCount is the number of workers that the algorithm is supposed to run on.
    public static int workerCount;

    // workerSet is the bitmask of workers currently have this node on their tarjanStack.
    // A concurrent bitset is required to counter the race conditions. If an non atomic
    // operation is done two workers might read the same value and output will be undefined.
    public ConcurrentBitSet workerSet;

    // parent denotes the parent of a node in the union find tree.
    private volatile Integer parent;

    public int parent() {
        return parentUpdater.get(this);
    }

    // parentUpdater is used to do atomic read/write/manipulations on the field parent.
    // Operations are atomic to this instance of UFNode.
    public static final AtomicReferenceFieldUpdater<UFNode, Integer> parentUpdater =
        AtomicReferenceFieldUpdater.newUpdater(UFNode.class, Integer.class, "parent");

    // listNext denotes the id of the node in the cyclic linked list structure.
    private volatile Integer listNext;

    public int listNext() {
        return listNextUpdater.get(this);
    }

    // listNextUpdater is used to do atomic read/write/manipulations on the field listNext.
    // Operations are atomic to this instance of UFNode.
    public static final AtomicReferenceFieldUpdater<UFNode, Integer> listNextUpdater =
        AtomicReferenceFieldUpdater.newUpdater(UFNode.class, Integer.class, "listNext");

    // UFStatus denotes the state of the union find node.
    // This state is in respect to other workers. It can take the following values:
    // 1. UFlive:
    //      Denoting no other worker is processing this node,
    //      though the worker might have it on it's tarjanStack.
    //      Hence, the node is available for processing.
    // 2. UFlock:
    //      Denoting that some other worker has a lock on it.
    //      Hence prevent this worker from modifications.
    // 3. UFdead:
    //      Denoting that the maximal SCC is found for this node.
    //      Hence, no need to process it.
    public enum UFStatus {
        UFlive, UFlock, UFdead;
    };

    // ufStatus represents the UFStatus of this node.
    private volatile UFStatus ufStatus;

    public UFStatus ufStatus() {
        return ufStatusUpdater.get(this);
    }

    // ufStatus is used to do atomic read/write/manipulations on the field ufStatus.
    // Operations are atomic to this instance of UFNode.
    public static final AtomicReferenceFieldUpdater<UFNode, UFStatus> ufStatusUpdater =
        AtomicReferenceFieldUpdater.newUpdater(UFNode.class, UFStatus.class, "ufStatus");

    // ListStatus denotes the state of union find node in the cyclic linked list.
    // It can take the following values:
    // 1. listLive:
    //      Denoting no other worker is processing this node
    //      and hence is available for seize.
    // 2. listLock:
    //      Denoting that some other worker is busy doing list operations
    //      with this node.
    // 3. listTomb:
    //      Denoting that node has been fully explored and can be removed from list.
    //      Note that we cannot make listNext null. Because while one worker might
    //      be exploring the listTomb node other might change its listNext to null
    //      thus breaking the cycle for the former worker.
    public enum ListStatus {
        listLive, listLock, listTomb;
    };

    // listStatus represents the ListStatus of this node in the cyclic linked list.
    private volatile ListStatus listStatus;

    public ListStatus listStatus() {
        return listStatusUpdater.get(this);
    }

    // listStatus is used to do atomic read/write/manipulations on the field listStatus.
    // Operations are atomic to this instance of UFNode.
    public static final AtomicReferenceFieldUpdater<UFNode, ListStatus> listStatusUpdater =
        AtomicReferenceFieldUpdater.newUpdater(UFNode.class, ListStatus.class, "listStatus");

    // Constructor.
    public UFNode() {
        this.workerSet = new ConcurrentBitSet(UFNode.workerCount);
        UFNode.parentUpdater.set(this, 0);
        UFNode.listNextUpdater.set(this, 0);
        // Initially every node is live.
        UFNode.ufStatusUpdater.set(this, UFStatus.UFlive);
        UFNode.listStatusUpdater.set(this, ListStatus.listLive);
    }
}
