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

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import no.uib.mayarobbestad.dagcentrality.datastructures.VertexCentralityPair;

public class DAGPageRankCentrality<V, E> implements VertexScoringAlgorithm<V, Double> {

    private static final int MAX_ITERATIONS_DEFAULT = 100;

    private Graph<V, E> graph;
    private Map<V, Double> scores;
    private int maxIterations;
    private boolean normalized;
    private VertexCentralityPair<V> pair;

    public DAGPageRankCentrality(Graph<V, E> graph) {
        this(graph, MAX_ITERATIONS_DEFAULT, true);
    }

    public DAGPageRankCentrality(Graph<V, E> graph, int maxIterations, boolean normalized) {
        this.graph = graph;
        this.scores = new HashMap<>();
        this.maxIterations = maxIterations;
        this.normalized = normalized;
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

        Set<V> sources = new HashSet<>();
        Set<V> sinks = new HashSet<>();

        HashMap<V, Double> totalWeight = new HashMap<>();

        List<V> topologicalList = new ArrayList<>();
        TopologicalOrderIterator<V, E> iterator = new TopologicalOrderIterator<>(copy);
        while (iterator.hasNext()) {
            topologicalList.add(iterator.next());
        }

        System.out.println(topologicalList);

        Map<V, Map<V, Double>> weightReceivedFromAncestor = new HashMap<>();
        for (V v : copy.vertexSet()) {
            weightReceivedFromAncestor.put(v, new HashMap<>());
            if (copy.inDegreeOf(v) == 0) {
                sources.add(v);
            }
        }

        for (V v : copy.vertexSet()) {
            if (copy.outDegreeOf(v) == 0) {
                sinks.add(v);
            }
            if (copy.inDegreeOf(v) == 0) {
                totalWeight.put(v, 1.0);
            } else {
                totalWeight.put(v, 0.0);
            }
            for (V s : sources) {
                // the source is their own ancestor
                if (v.equals(s)) {
                    weightReceivedFromAncestor.get(v).put(s, 1.0);
                } else {
                    weightReceivedFromAncestor.get(v).put(s, 0.0);
                }
            }
        }
        // number of iterations in PageRank
        for (int i = 0; i < maxIterations; i++) {
            // forward
            for (int j = 0; j < topologicalList.size(); j++) {

                V v = topologicalList.get(j);
                // the score this vertex will distribute to their outdegree neighbours

                for (E edge : copy.outgoingEdgesOf(v)) {
                    V w = Graphs.getOppositeVertex(copy, edge, v);

                    // iterate over all the scores of v received from each source vertex
                    Double weight = totalWeight.get(w);
                    for (V s : sources) {
                        Double scoreFromGivenSource = weightReceivedFromAncestor.get(v).get(s);
                        // no need to do all the rest if the score is 0
                        if (scoreFromGivenSource > 0) {
                            // v distributes the score received from a source evenly amongst their outdegree
                            // neighbourhood
                            Double toDistributeFromSource = scoreFromGivenSource / (double) copy.outDegreeOf(v);
                            // updates the weight received from the source vertex s
                            Double updateScore = weightReceivedFromAncestor.get(w).get(s) + toDistributeFromSource;
                            weightReceivedFromAncestor.get(w).put(s, updateScore);
                            weight += toDistributeFromSource;
                        }
                    }
                    totalWeight.put(w, weight);
                }
            }
            System.out.println("iteration " + i + ":" + weightReceivedFromAncestor);
            scores.putAll(totalWeight);
            for (V v : totalWeight.keySet()) {
                totalWeight.put(v, 0.0);
            }

            if (i + 1 < maxIterations) {
                // update the weight each source at a time
                for (V source : sources) {
                    Double newWeight = 0.0;
                    for (V sink : sinks) {
                        // adds part/whole back to the source
                        newWeight += weightReceivedFromAncestor.get(sink).get(source) / scores.get(sink);
                    }
                    totalWeight.put(source, newWeight);
                }
                // reset scores to be ready for the next iteration
                for (V v : copy.vertexSet()) {
                    for (V s : sources) {
                        // receive the score of the previous iteration
                        if (v.equals(s)) {
                            weightReceivedFromAncestor.get(v).put(s, totalWeight.get(s));
                        } else {
                            weightReceivedFromAncestor.get(v).put(s, 0.0);
                        }
                    }
                }
            }
        }
    }

    private void resetVisited(Map<V, Boolean> visited) {
        for (V vertex : graph.vertexSet()) {
            visited.put(vertex, false);
        }
    }

}
