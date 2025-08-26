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
import org.jgrapht.graph.SimpleDirectedGraph;

public class DAGPageRankCentrality<V, E> implements VertexScoringAlgorithm<V, Double> {

    private Graph<V, E> graph;
    private Map<V, Double> scores;
    int ITERATIONS = 3;

    public DAGPageRankCentrality(Graph<V, E> graph) {
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
     * The actual implementation of the centrality measure
     */
    private void run() {
        Graph<V, E> copy = new SimpleDirectedGraph<>(graph.getVertexSupplier(), graph.getEdgeSupplier(), false);

        Graphs.addGraph(copy, graph);

        // TODO: make field variable

        // init
        Map<V, Double> weights = new HashMap<>();

        Queue<V> toDistribute = new LinkedList<>();

        Set<V> sources = new HashSet<>();
        Set<V> sinks = new HashSet<>();

        Map<V, Set<V>> sourceAncestors = new HashMap<>();

        for (V v : copy.vertexSet()) {
            if (copy.outDegreeOf(v) == 0) {
                sinks.add(v);
            }
            if (copy.inDegreeOf(v) == 0) {
                weights.put(v, 1.0);
                toDistribute.add(v);
                sources.add(v);
            } else {
                weights.put(v, 0.0);
            }
            sourceAncestors.put(v, new HashSet<>());
        }

        // number of iterations in PageRank
        for (int i = 0; i < ITERATIONS; i++) {
            // forward
            while (!toDistribute.isEmpty()) {
                V v = toDistribute.poll();

                for (E edge : copy.outgoingEdgesOf(v)) {
                    V w = Graphs.getOppositeVertex(copy, edge, v);
                    // V w = copy.getEdgeTarget(edge);

                    Double currentWeight = weights.get(w);
                    Double ancestorWeight = weights.get(v) / copy.outDegreeOf(v);

                    // weight(u)+=weight(v)/deg(v)
                    weights.put(w,
                            currentWeight + ancestorWeight);
                    // no duplicates, but perhaps change this to visited
                    // Since we are working with a DAG, this solution can be good enough
                    // since there are no cycles in the graph
                    if (!toDistribute.contains(w)) {
                        toDistribute.add(w);
                    }
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
            if (i + 1 < ITERATIONS) {
                // reset weights
                for (V v : weights.keySet()) {
                    weights.put(v, 0.0);
                }
                // backwards
                for (V v : sinks) {
                    Integer numSources = sourceAncestors.get(v).size();
                    // Double weight = weights.get(v) ;
                    Double weight = 1.0;
                    for (V source : sourceAncestors.get(v)) {
                        Double temp = weights.get(source);
                        weights.put(source, temp + weight / numSources); // normalize
                    }
                }
            }
            toDistribute.addAll(sources);
        }
    }
}
