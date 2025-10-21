package no.uib.mayarobbestad.dagcentrality.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.traverse.TopologicalOrderIterator;

/**
 * This centrality calculates the score of a vertex based on the number of paths
 * a vertex is on.
 * Inspired by betweenness centrality, but instead of finding the shortest paths
 * from every vertex to
 * all other vertices, we calculate the score based on the number of shortest
 * paths from all sources to sinks
 */
public class APSPSourceSinkBetweenness<V, E> implements VertexScoringAlgorithm<V, Long> {

    private Graph<V, E> graph;
    private Map<V, Long> scores;
    private Map<V, Map<V, Integer>> dist;
    private Map<V, Set<V>> P;

    public APSPSourceSinkBetweenness(Graph<V, E> graph) {
        this.graph = graph;
        this.scores = new HashMap<>();
        this.P = new HashMap<>();
        for (V v : graph.vertexSet()) {
            P.put(v, new HashSet<>());
        }
        run();
    }

    @Override
    public Map<V, Long> getScores() {
        if (scores == null) {
            run();
        }
        return Collections.unmodifiableMap(scores);
    }

    @Override
    public Long getVertexScore(V v) {
        if (!graph.containsVertex(v)) {
            throw new IllegalArgumentException("Cannot return score of unknown vertex");
        }
        if (scores == null) {
            run();
        }
        return getScores().get(v);
    }

    /**
     * The actual implementation of the algorithm
     */
    private void run() {
        System.out.println(graph);
        // sort vertices in topological ordering

        Set<V> sources = new HashSet<>();
        Set<V> internal = new HashSet<>();
        Set<V> sinks = new HashSet<>();

        List<V> topologicalList = new ArrayList<>(graph.vertexSet().size());
        TopologicalOrderIterator<V, E> iterator = new TopologicalOrderIterator<>(graph);

        while (iterator.hasNext()) {
            V current = iterator.next();
            topologicalList.add(current);
            // initialize source, internal and sink
            if (graph.inDegreeOf(current) == 0) { // source
                sources.add(current);
            } else if (graph.outDegreeOf(current) == 0) { // sink
                sinks.add(current);
            } else {
                internal.add(current); // internal
            }
            scores.put(current, (long) 0);
        }
        System.out.println("Sources:" + sources);
        System.out.println("sinks" + sinks);
        System.out.println("internal" + internal);

        this.dist = new HashMap<>();
        // initialize APSP
        for (int i = 0; i < topologicalList.size(); i++) {
            V v = topologicalList.get(i);
            bfs(v);
        }
        System.out.println(topologicalList);
        System.out.println(dist);

        APSPNotNormalized(sources, internal, sinks);
    }

    private void APSPNotNormalized(Set<V> sources, Set<V> internal, Set<V> sinks) {
        for (V s : sources) {
            for (V t : sinks) {
                if (!dist.get(s).containsKey(t)) // t not reachable from s
                    continue;
                for (V v : internal) {
                    if (!dist.get(s).containsKey(v) || !dist.get(v).containsKey(t))
                        continue;
                    if (dist.get(s).get(v) + dist.get(v).get(t) == dist.get(s).get(t))

                        scores.put(v, scores.get(v) + 1);
                }
            }
        }
    }

    private void bfs(V start) {

        Stack<V> S = new Stack<>();

        Set<V> visited = new HashSet<>();
        Queue<V> toSearch = new LinkedList<>();
        // key=child, value=parent
        Map<V, V> parent = new HashMap<>();
        Map<V, Integer> distFromV = new HashMap<>();

        parent.put(start, start);
        toSearch.add(start);
        // distance from itself is 0, which will be updated in the while loop
        distFromV.put(start, -1);

        while (!toSearch.isEmpty()) {

            V current = toSearch.poll();
            visited.add(current);
            // distance of the parent of current vertex + 1
            int currentLevel = distFromV.get(parent.get(current)) + 1;
            // shortest path from v to current vertex
            distFromV.put(current, currentLevel);
            for (E e : graph.outgoingEdgesOf(current)) {
                V w = Graphs.getOppositeVertex(graph, e, current);
                System.out.println("v,w" + start + "," + w + " edge:" + e);

                if (!visited.contains(w)) {
                    // current vertex is the parent of w
                    parent.put(w, current);
                    toSearch.add(w);
                }
            }
        }
        System.out.println("v:" + start + ":" + distFromV);

        this.dist.put(start, distFromV);
    }

}
