Future Work
===========

This are just ideas to improve the performance. They haven't been tested and might not improve the performance. But still I am mentioning it here.

- Initiallty we queue all the init nodes into the workers queue and re-queue this nodes if we find that we still have more workers ideal. But it can be the case that one of workers finish processing a node very early. Hence, we can try to re-queue some another node into this worker.

- As we can see that each worker processes a node's outgoing edges in a random fashion. This method is not uniform as each worker just does a constant cyclic shift. We can try to make this exploration more uniform.

- Another way to improve performance would be to remove the cyclic linked list (helpful in deleting nodes). Currently, this prevents us from making implementation lockless. But removing it and finding an efficient way to delete nodes might give us a lockless implementation.
