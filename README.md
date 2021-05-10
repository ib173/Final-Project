# Large Scale Data Processing: Final Project
## Inability to complete the algorithm
  *We originally attempted to implement the Hobcroft-Karp algorithm by forcing the graphs into the bipartite form, however we soon realized that this skews all the data and leaves us with some rather useless matchings. In doing so, we utilized the JgraphT library in Java to test out the matchings prior writing implicit functions in Scala leveraging the library.
  *We then attempted to implement the bidding variant of Luby's algorithm for general graphs, but became stumped when it comes to the process of discerning the maximal matching prior to the maximum matching. Our attempt at the code is in the main.scala file although it does work. We elaborated on our attempt at implementation below.
## Algorithms Considered
Interestingly, many of the algorithms we researched utilize very similar processes with rather small changes. For example, the Gabow (1976) algorithm improves on the Edmonds algorithm with a run time of V^4 by eliminating the Blossom expansion, an algorithm in and of itself that could be altered to solve this problem. In this way, many of these algorithms are iterative upon the last, which makes for an interesting continuation.

* Hopcroft-Karp Algorithm
    Originally gravitated towards this algorithm due to its ability to solve both bipartite and non-bipartite graphs. Generally, the algorithm repeatedly increases the size of a partial matching by finding augmenting paths. The process of discerning these augmenting paths is different for bipartite and non-bipartite or, more specifically, general graphs. Namely, the process of discerning augmenting paths for a general graph proves rather difficult.
      The general pseudocode of the algorithm given a bipartite graph is as follows:
      ###### 1) Initialize Maximal Matching M as empty.
      ###### 2) While there exists an Augmenting Path p Remove matching edges of p from M and add not-matching edges of p to M (This increases size of M by 1 as p starts and ends with a free vertex)
      3) Return M.
    A potential solution to the aforementioned problem would be to convert our general graph to a bipartite graph. The problem that arises, however, is the process of ‘converting’ a general graph to bipartite would greatly alter our original graph, figuratively “cutting down” branches and edges to fit the necessities of a bipartite graph, namely a graph whose vertices can be divided into two disjoint and independent sets U and V such that every edge connects a vertex U to one in V.

* Alon, Babai, Itai
    We considered using the Israeli-Itai algorithm to find the maximal matching prior to augmenting along the matchings to find the maximum matching. The algorithm finds a maximal matching that is at least ½ of the number of matching as the maximum matching of the graph. Due to the nature of this algorithm it would have to be combined with one of the mentioned maximum matching algorithms to complete the assigned problem, namely implementing augmenting paths on top of the algorithm similar to the Hopcroft-Karp algorithm.

* Dual ascent
* Luby’s (bidding variant)
    A general class of algorithms for the bipartite matching problem are “auction algorithms”. These algorithms interpret the input bipartite graph as a collection of bidders on one side and items on the other side, and hold an auction for finding a welfare-maximizing assignment of items to bidders which translates to a maximum matching of the input graph.
    Suppose in every iteration of the auction algorithm, we pick a maximal matching in the subgraph consisting of the unallocated bidders and all their minimum-price items; then, the auction terminates in a (1 − ε)-approximate matching of the input graph in only O(1/ε^2 ) iterations.
      Our general process for in attempting to implement this algorithm is as follows:
      ##1.) treat all edges in the input graph as undirected 
      ## 2.) all vertices are in the candidate MIS at the beginning 
      *3.) compute working graph, which has as a vertex attribute of “status” which corresponds unknown or known 
      4.) remove the isolated vertices from the working graph
      5.) count the edges instead of vertices to help us avoid unnecessary iterations in the main loop
      6.) implement the following aggregate messages:
            A. mark each unknown vertex as “Selected” on a random basis
            B. the random decision can be discerned by a simple .5 p val “coin flip”
            C. Mark the neighbors of each Selected vertex as Unknown
            We cannot exclude vertices at this stage since otherwise we’ll probably exclude too many and skew our results
      7.) identify the vertices to exclude, namely vertices near Selected vertices
      8.) remove excluded vertices from candidate MIS
      9.) keep non-isolated Unknown vertices in working graph
      10.) remove the vertices that received no messages
      11.) eventually, all vertices near Selected vertices are excluded from the working graph, but since all the vertices were in the candidate MIS at the beginning, we’re left with the Selected vertices and the Unknown vertices.
  Advantages:
    By implementing the matching an auction/bidding variant on top of Luby’s algorithm, we can gain a simple means of boosting the ½-approximation of maximal matching to a (1 − ε) approximation.
    All we need to do to implement this approach is to be able to maintain and “status” of the items, and run maximal matching on the subgraph of the input between the unknown and known items.
    Similarly to our failure in implementing the Hopcroft-Karp Algorithm, we struggled to figure out and implement the two step process of creating our maximal matching prior to running our algorithm for maximum matching. We originally were trying to implement this in one step, however we realized that would be almost impossible.
