package no.uib.mayarobbestad.dagcentrality.algorithms;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class DAGPageRankCentrality<V, E> implements VertexScoringAlgorithm<V, Double> {

    private static final int MAX_ITERATIONS_DEFAULT = 100;

    private Graph<V, E> graph;
    private Map<V, Double> scores;
    private int maxIterations;
    private boolean normalized;

    public DAGPageRankCentrality(Graph<V, E> graph) {
        this(graph, MAX_ITERATIONS_DEFAULT, true);
    }

    public DAGPageRankCentrality(Graph<V, E> graph, int maxIterations, boolean normalized) {
        this.graph = graph;
        this.scores = new HashMap<>();
        this.maxIterations = maxIterations;
        this.normalized = true;
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
     * The actual implementation of the centrality measure
     */
    private void run() {
        Graph<V, E> copy = new SimpleDirectedGraph<>(graph.getVertexSupplier(), graph.getEdgeSupplier(), false);

        Graphs.addGraph(copy, graph);

        // init

        Set<V> sources = new HashSet<>();
        Set<V> sinks = new HashSet<>();
        Map<V, Set<V>> sourceAncestors = new HashMap<>();
        Map<V, Double> weights = new HashMap<>();
        Queue<V> Q = new LinkedList<>(); // vertices that have not distributed their scores yet
        Map<V, Boolean> visited = new HashMap<V, Boolean>();

        resetVisited(visited);

        for (V v : copy.vertexSet()) {
            if (copy.outDegreeOf(v) == 0) {
                sinks.add(v);
            }
            if (copy.inDegreeOf(v) == 0) {
                weights.put(v, 1.0);
                Q.add(v);
                sources.add(v);
            } else {
                weights.put(v, 0.0);
            }
            sourceAncestors.put(v, new HashSet<>());
        }

        // number of iterations in PageRank
        for (int i = 0; i < maxIterations; i++) {
            TopologicalOrderIterator<V, E> iterator = new TopologicalOrderIterator<>(copy);

            // forward
            while (iterator.hasNext()) {
                V v = iterator.next();
                visited.put(v, true);

                for (E edge : copy.outgoingEdgesOf(v)) {
                    V w = Graphs.getOppositeVertex(copy, edge, v);

                    // weight(u)+=weight(v)/deg(v)
                    Double currentWeight = weights.get(w);
                    Double ancestorWeight = weights.get(v) / copy.outDegreeOf(v);
                    weights.put(w,
                            currentWeight + ancestorWeight);

                    // adding root ancestors
                    Set<V> temp = sourceAncestors.get(w);
                    if (sources.contains(v)) {
                        temp.add(v);
                    } else {
                        for (V a : sourceAncestors.get(v)) {
                            temp.add(a);
                        }
                    }
                }
            }

            scores.putAll(weights);

            // prep for the next iteration
            if (i + 1 < maxIterations) {

                // reset weights
                for (V v : weights.keySet()) {
                    weights.put(v, 0.0);
                }
                // backwards
                for (V v : sinks) {
                    Integer numSources = sourceAncestors.get(v).size();

                    /*
                     * Double weight;
                     * if (normalized) {
                     * weight = 1.0;
                     * } else {
                     * weight = scores.get(v);
                     * }
                     */
                    Double weight = scores.get(v);

                    for (V source : sourceAncestors.get(v)) {
                        Double temp = weights.get(source);
                        weights.put(source, temp + weight / numSources); // normalize
                    }
                }
            }
            Q.addAll(sources);
        }
    }

    private void resetVisited(Map<V, Boolean> visited) {
        for (V vertex : graph.vertexSet()) {
            visited.put(vertex, false);
        }
    }

}
