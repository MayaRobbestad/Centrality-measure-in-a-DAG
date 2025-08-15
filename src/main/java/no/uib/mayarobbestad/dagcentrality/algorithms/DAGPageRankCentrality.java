package no.uib.mayarobbestad.dagcentrality.algorithms;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;

public class DAGPageRankCentrality<V, E> implements VertexScoringAlgorithm<V, Double> {

    private Graph<V, E> graph;
    private Map<V, Double> scores;

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
        // TODO: make field variable
        int iterations = 1;

        // init
        Map<V, Double> weights = new HashMap<>();
        Map<V, Double> copyOfWeights = new HashMap<>();
        Queue<V> toDistribute = new LinkedList<>();
        Set<V> sources = new HashSet<>();
        Set<V> sinks = new HashSet<>();
        Map<V, Set<V>> sourceAncestors = new HashMap<>();

        for (V v : graph.vertexSet()) {
            if (graph.inDegreeOf(v) == 0) {
                weights.put(v, 1.0);
                toDistribute.add(v);
                sources.add(v);
            } else {
                weights.put(v, 0.0);
            }
            if (graph.outDegreeOf(v) == 0) {
                sinks.add(v);
            }
            sourceAncestors.put(v, new HashSet<>());
        }

        // number of iterations in PageRank
        for (int i = 0; i < iterations; i++) {
            // forward
            while (!toDistribute.isEmpty()) {
                V v = toDistribute.poll();
                for (E edge : graph.outgoingEdgesOf(v)) {
                    V w = graph.getEdgeTarget(edge);
                    Double currentWeight = weights.get(w);
                    // weight(u)+=weight(v)/deg(v)
                    weights.put(w,
                            currentWeight + weights.get(v) / graph.outDegreeOf(v));
                    toDistribute.add(w);
                    if (sources.contains(v)) {
                        Set<V> temp = sourceAncestors.get(w);
                        temp.add(v);
                        sourceAncestors.put(w, temp);
                    } else {
                        Set<V> ancestors = sourceAncestors.get(v); // get the sources of neighbour
                        ancestors.addAll(sourceAncestors.get(w)); // merge
                        sourceAncestors.put(w, ancestors);
                    }
                }
                // weight has flowed from that vertex, the weights are reset
                copyOfWeights.put(v, weights.get(v));
                System.out.println(weights);
                weights.put(v, 0.0);
            }

            // backwards
            for (V v : sinks) {
                Double numSources = (double) sourceAncestors.get(v).size();
                // Double weight = weights.get(v) ;
                Double weight = 1.0;
                for (V source : sourceAncestors.get(v)) {
                    Double temp = weights.get(source);
                    weights.put(source, temp + weight / numSources);
                }
            }
        }
        // scores.putAll(copyOfWeights);
        scores.putAll(weights);
    }
}
