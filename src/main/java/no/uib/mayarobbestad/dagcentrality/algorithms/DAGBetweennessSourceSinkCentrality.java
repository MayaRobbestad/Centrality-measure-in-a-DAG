package no.uib.mayarobbestad.dagcentrality.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

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

            Stack<V> S = new Stack<>(); // find shortest path for all vertices s in V
            Map<V, List<V>> predecessors = new HashMap<>();
            Map<V, Double> sigma = new HashMap<>();
            Map<V, Integer> dist = new HashMap<>();
            Queue<V> Q = new LinkedList<>();

            for (V t : graph.vertexSet()) {
                sigma.put(t, 0.0);
                dist.put(t, -1);
                predecessors.put(t, new ArrayList<>());
            }

            sigma.put(s, 1.0);
            dist.put(s, 0);
            Q.add(s);

            while (!Q.isEmpty()) {
                V v = Q.poll();
                S.add(v);
                for (E e : graph.outgoingEdgesOf(v)) {
                    V w = graph.getEdgeTarget(e);
                    // if w is found for the first time
                    if (dist.get(w) <= 0) {
                        Q.add(w);
                        dist.put(w, dist.get(v) + 1);
                    }
                    // shortest path to w via v
                    if (dist.get(w) == dist.get(v) + 1) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        List<V> temp = predecessors.get(w);
                        temp.add(v);
                        predecessors.put(w, temp);
                    }
                }
            }
            Map<V, Double> delta = new HashMap<>();
            for (V v : graph.vertexSet()) {
                delta.put(v, 0.0);
            }
            while (!S.isEmpty()) {
                V w = S.pop();
                for (V v : predecessors.get(w)) {
                    Double temp = delta.get(v) + delta.get(v) / delta.get(w) * (1 + delta.get(w));
                    delta.put(w, temp);
                }
                if (!w.equals(s)) {
                    scores.put(w, scores.get(w) + delta.get(w));
                }

            }
        }
    }
}
