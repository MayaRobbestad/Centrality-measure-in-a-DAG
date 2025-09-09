package no.uib.mayarobbestad.dagcentrality.algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;

/**
 * This centrality calculates the score of a vertex based on the number of paths
 * a vertex is on.
 * Inspired by betweenness centrality, but instead of finding the shortest paths
 * from every vertex to
 * all other vertices, we calculate the score based on the number of shortest
 * paths from all sources to sinks
 */
public class DAGBetweennessSourceSinkCentrality<V, E> implements VertexScoringAlgorithm<V, Double> {

    private Graph<V, E> graph;
    private Map<V, Double> scores;

    public DAGBetweennessSourceSinkCentrality(Graph<V, E> graph) {
        this.graph = graph;
        this.scores = new HashMap<>();
        run();
    }

    @Override
    public Map<V, Double> getScores() {
        if (scores == null) {
            run();
        }
        return Collections.unmodifiableMap(scores);
    }

    @Override
    public Double getVertexScore(V v) {
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

        Set<V> sources = new HashSet<>();
        for (V v : graph.vertexSet()) {
            scores.put(v, 0.0);
            if (graph.inDegreeOf(v) == 0) {
                sources.add(v);
            }
        }

        // Calculate single source shortest paths for all vertices s in sources
        for (V s : sources) {

            ArrayDeque<V> S = new ArrayDeque<>(); // find shortest path for all vertices s in V
            Map<V, List<V>> predecessors = new HashMap<>();
            Map<V, Long> sigma = new HashMap<>();
            Map<V, Double> dist = new HashMap<>();
            Queue<V> Q = new LinkedList<>();

            for (V t : graph.vertexSet()) {
                sigma.put(t, 0l);
                dist.put(t, -1.0);
                predecessors.put(t, new ArrayList<>());
            }

            sigma.put(s, 1l);
            dist.put(s, 0.0);
            Q.add(s);

            while (!Q.isEmpty()) {
                V v = Q.poll();
                S.push(v);
                for (E e : graph.outgoingEdgesOf(v)) {
                    V w = graph.getEdgeTarget(e);
                    // if w is found for the first time
                    if (dist.get(w) < 0) {
                        Q.add(w);
                        dist.put(w, dist.get(v) + 1);
                    }
                    // shortest path to w via v
                    // Only calculate sigma if v is a predecessor of w
                    if (dist.get(w) == dist.get(v) + 1) {
                        Long temp = sigma.get(w);
                        sigma.put(w, temp + sigma.get(v));
                        // overflow?
                        predecessors.get(w).add(v);
                    }
                }
            }

            // 2. sum all pair-dependencies
            Map<V, Double> delta = new HashMap<>();
            for (V v : graph.vertexSet()) {
                delta.put(v, 0.0);
            }
            while (!S.isEmpty()) {
                V w = S.pop();
                for (V v : predecessors.get(w)) {
                    Double temp = delta.get(v)
                            + (sigma.get(v).doubleValue() / sigma.get(w).doubleValue())
                                    * (1 + delta.get(w));
                    delta.put(v, temp);
                }
                if (!w.equals(s)) {
                    scores.put(w, scores.get(w) + delta.get(w));
                }
            }
        }
    }
}
