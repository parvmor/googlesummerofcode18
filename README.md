A Concurrent Strongly Connected Components Algorithm
====================================================

This repository contains an implementation of a scalable algorithm to find strongly connected components as proposed in the paper [Multi-core on-the-fly SCC decomposition](https://dl.acm.org/citation.cfm?id=2851161). Core of the implementation is in Java and resides in the package `tarjanUF` which can be found in `src`. This project was done as part of *Google Summer of Code 2018* under the organization *tlaplus*. Thanks to *tlaplus* and my mentors *Markus Kuppe* and *Stephan Merz* for providing me with this opportunity.

The implementation was ran on several graphs from the BEEM dataset (divine model graphs) with number of threads varying from 1 to 32. The result of the experiment resides in `experiments/plotter/plot_multiple_runs.pdf`. A concurrent union find structure proposed in the paper [A Randomized Concurrent Algorithm for Disjoint Set Union](https://dl.acm.org/citation.cfm?id=2933108) was tried to improve the performance. This results can found in
`experiments/plotter/plot_path_splitting.pdf` in `tarjanRCUF` branch. Box plots represent the simulations of new structure whereas points represent results of old structure.

To reproduce the results refer to `experiments/benchmark` script.

A documentation of summary of the algorithm can be found at: `doc/UnionFindConcurrentSCC.pdf`. The algorithm depends on random exploration of graph and takes advantage of already discovered cycles in the graph by other threads to avoid re-exploration. Following example illustrates this:

- Say worker 1 has explored an SCC such that two nodes `A` and `C` are in the same SCC.
- Consider another worker (say 2) explores `A -> B -> C`.
- This implies that there is a cycle of the following form: `A -> B -> C -> (some path discovered by worker 1) -> A`.


Getting Started
===============

- Install `oraclejdk` as a dependency.
- Create a directory: `mkdir bin`
- To compile the project: `make compile`
- To run the project: `make run GRAPH=<graph> THREADS=<#threads> INIT=<initNodes>`
- To clean the project: `make clean`

Note that `<graph>` is provided in an edge list representation. `<initNodes>` is the list of initial nodes from where DFS will start. This list should ensure that entire graph can be explored. Use `divineParser/augment` to make the nodes contiguous integers (starting from 1) if they are not already.

Implementation
==============

The package `tarjanUF` contains several classes which are described as follows:

- GraphNode: A node in a graph whose `arcs` can tell all the outgoing edges of this node. The property `id` identifies this node uniquely. All of the methods of this class are standard.
- Graph: A graph of `GraphNode`s. It conatins a `HashMap` mapping each node identifier to the node itself. All of the methods of this class are standard.
- UFNode: This class is an implementation of nodes of an augmented concurrent union-find data structure. Refer to `doc/UnionFindConcurrentSCC.pdf` for an understanding of the data-structure. All properties of this class are atomic/volatile so as to avoid race conditions.
- UF: This contains methods of manipulating the data structure. It involves standard union find operations along with some cyclic list operations. The latter operations involve merging two lists, remove an element from the list and marking a node dead in the list. The class also contains several auxilary operations to aid in locking of nodes.
- ConcurrentFastSCC: This is the executor class of the algorithm which initiates the finding of SCCs and returns sets of nodes that belong in the same SCC.
- SCCWorker: The `run` method of this class implements the iterative version of SCC finding algorithm which is quite similar to the Tarjan's sequential algorithm.
- ConcurrentBitSet: Used to maintain the set of workers an UFNode is being processed by. A non concurrent bitset would result in race conditions.

Note
====

Due to certain issues we can only provide a snapshot of the current repository. We hope to make rest of the work public as and when we have resolved the issues. Thanks for your co-operation.
