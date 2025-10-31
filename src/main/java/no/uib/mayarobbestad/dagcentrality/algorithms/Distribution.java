package no.uib.mayarobbestad.dagcentrality.algorithms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class Distribution<V, E> implements VertexScoringAlgorithm<V, BigDecimal> {

    private static final int MAX_ITERATIONS_DEFAULT = 100;

    private int ROUNDINGNUM = 1000;

    private Graph<V, E> graph;
    private Map<V, BigDecimal> scores;
    private int maxIterations;

    public Distribution(Graph<V, E> graph) {
        this(graph, MAX_ITERATIONS_DEFAULT);
    }

    public Distribution(Graph<V, E> graph, int maxIterations) {
        this.graph = graph;
        this.scores = new HashMap<>();
        this.maxIterations = maxIterations;
        // run();
    }

    @Override
    public Map<V, BigDecimal> getScores() {
        if (scores == null) {
            run();
        }
        return Collections.unmodifiableMap(scores);
    }

    @Override
    public BigDecimal getVertexScore(V v) {
        // stupid solution, however the algorithm should run when detVertexScore is
        // called
        run();
        if (!graph.containsVertex(v)) {
            throw new IllegalArgumentException("Cannot return score of unknown vertex");
        }
        return getScores().get(v);
    }

    /**
     * The actual implementation of the centrality measure
     */
    private void run() {

        // Initialize
        Graph<V, E> copy = new SimpleDirectedGraph<>(graph.getVertexSupplier(), graph.getEdgeSupplier(), false);
        Graphs.addGraph(copy, graph);

        Set<V> sources = new HashSet<>();
        Set<V> sinks = new HashSet<>();

        HashMap<V, BigDecimal> totalWeight = new HashMap<>();

        List<V> topologicalList = new ArrayList<>();
        TopologicalOrderIterator<V, E> iterator = new TopologicalOrderIterator<>(copy);
        while (iterator.hasNext()) {
            topologicalList.add(iterator.next());
        }

        Map<V, Map<V, BigDecimal>> weightReceivedFromAncestor = new HashMap<>();
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
                totalWeight.put(v, new BigDecimal("1"));
                scores.put(v, new BigDecimal("1"));
            } else {
                totalWeight.put(v, new BigDecimal("0"));
                scores.put(v, new BigDecimal("0"));
            }
            for (V s : sources) {
                // the source is their own ancestor
                if (v.equals(s)) {
                    weightReceivedFromAncestor.get(v).put(s, new BigDecimal("1"));
                } else {
                    weightReceivedFromAncestor.get(v).put(s, new BigDecimal("0"));
                }
            }
        }

        // number of iterations in PageRank
        for (int i = 1; i <= maxIterations; i++) {

            forwardFlow(copy, sources, totalWeight, topologicalList, weightReceivedFromAncestor);

            scores.putAll(totalWeight);
            for (V v : totalWeight.keySet()) {
                totalWeight.put(v, new BigDecimal("0"));
            }

            if (i < maxIterations) {
                backwardFlow(copy, sources, sinks, totalWeight, weightReceivedFromAncestor);
            }
        }
    }

    /**
     * All sink vertices send the proportional score received from the source
     * vertices back to the source vertex.
     * 
     * @param copy
     * @param sources
     * @param sinks
     * @param totalWeight
     * @param weightReceivedFromAncestor
     */
    private void backwardFlow(Graph<V, E> copy, Set<V> sources, Set<V> sinks, HashMap<V, BigDecimal> totalWeight,
            Map<V, Map<V, BigDecimal>> weightReceivedFromAncestor) {

        // update the weight each source at a time
        for (V source : sources) {
            BigDecimal newWeight = new BigDecimal("0");
            for (V sink : sinks) {
                // adds part divided by whole back to the source
                newWeight = newWeight.add(
                        weightReceivedFromAncestor.get(sink).get(source)
                                .divide(scores.get(sink),
                                        ROUNDINGNUM, RoundingMode.HALF_EVEN));
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
                    weightReceivedFromAncestor.get(v).put(s, new BigDecimal("0"));
                }
            }
        }
    }

    /**
     * Each vertex distributes the score given from each source vertex equally to
     * their out neighbours.
     * 
     * @param copy
     * @param sources
     * @param totalWeight
     * @param topologicalList
     * @param weightReceivedFromAncestor
     */
    private void forwardFlow(Graph<V, E> copy, Set<V> sources, HashMap<V, BigDecimal> totalWeight,
            List<V> topologicalList, Map<V, Map<V, BigDecimal>> weightReceivedFromAncestor) {

        for (int j = 0; j < topologicalList.size(); j++) {
            V v = topologicalList.get(j);
            // the score this vertex will distribute to their outdegree neighbours
            for (E edge : copy.outgoingEdgesOf(v)) {
                V w = Graphs.getOppositeVertex(copy, edge, v);
                // iterate over all the scores of v received from each source vertex
                BigDecimal weight = totalWeight.get(w);
                for (V s : sources) {
                    BigDecimal scoreFromGivenSource = weightReceivedFromAncestor.get(v).get(s);
                    // no need to do all the rest if the score is 0
                    // 1 if scoreFromGivenSource > 0, if <
                    if (scoreFromGivenSource.compareTo(new BigDecimal("0")) > 0) {
                        // v distributes the score received from a source evenly amongst their outdegree
                        // neighbourhood
                        BigDecimal toDistributeFromSource = scoreFromGivenSource
                                .divide(new BigDecimal(copy.outDegreeOf(v)), ROUNDINGNUM, RoundingMode.HALF_EVEN);

                        BigDecimal updateScore = weightReceivedFromAncestor.get(w).get(s)
                                .add(toDistributeFromSource);
                        weightReceivedFromAncestor.get(w).put(s, updateScore);
                        weight = weight.add(toDistributeFromSource);
                    }
                }
                totalWeight.put(w, weight);
            }
        }
    }
}
