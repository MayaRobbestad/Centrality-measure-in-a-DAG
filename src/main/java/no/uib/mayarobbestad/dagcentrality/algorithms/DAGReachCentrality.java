package no.uib.mayarobbestad.dagcentrality.algorithms;

import java.math.BigDecimal;
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

public class DAGReachCentrality<V, E> implements VertexScoringAlgorithm<V, BigDecimal> {

    private Graph<V, E> graph;
    private Map<V, BigDecimal> scores;
    private boolean normalize;
    private CentralityState state;

    public DAGReachCentrality(Graph<V, E> graph, boolean normalize) {
        this.graph = graph;
        this.scores = new HashMap<>();
        this.normalize = normalize;
        this.state = CentralityState.REACH;
        run();
    }

    /**
     * When can reach is true, the centrality score is based on how many vertices a
     * vertex can reach
     * If it is false, then the centrality score is based on how many vertices the
     * vertex has been reached by.
     * 
     * @param graph
     * @param normalize
     * @param canReach
     */
    public DAGReachCentrality(Graph<V, E> graph, boolean normalize, boolean canReach) {
        this.graph = graph;
        this.scores = new HashMap<>();
        this.normalize = normalize;
        if (canReach) {
            this.state = CentralityState.REACH;
        } else {
            this.state = CentralityState.DEPENDENCY;
        }
        run();
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
        if (!graph.containsVertex(v)) {
            throw new IllegalArgumentException("Cannot return score of unknown vertex");
        }
        return getScores().get(v);
    }

    private void run() {
        Graph<V, E> copy = new SimpleDirectedGraph<>(graph.getVertexSupplier(), graph.getEdgeSupplier(), false);
        Graphs.addGraph(copy, graph);

        Map<V, Set<V>> reached = new HashMap<>();

        for (V v : copy.vertexSet()) {
            reached.put(v, new HashSet<>());
        }

        List<V> topologicalList = new ArrayList<>();
        TopologicalOrderIterator<V, E> iterator = new TopologicalOrderIterator<>(copy);
        while (iterator.hasNext()) {
            topologicalList.add(iterator.next());
        }

        if (state == CentralityState.REACH) {
            reach(copy, reached, topologicalList);
        } else {
            dependency(copy, reached, topologicalList);
        }

        System.out.println(reached);

        for (V v : copy.vertexSet()) {
            if (!normalize) {
                BigDecimal score = new BigDecimal(reached.get(v).size());
                scores.put(v, score);
            }
            if (normalize) {
                BigDecimal n = new BigDecimal(copy.vertexSet().size());
                BigDecimal score = new BigDecimal(reached.get(v).size());
                scores.put(v, score.divide(n));
            }
        }
    }

    /**
     * This method finds all the vertices w that can reach vertex v.
     * Vertex v keeps track of all the vertices that can reach this vertex
     * 
     * @param copy
     * @param reached
     * @param topologicalList
     */
    private void dependency(Graph<V, E> copy, Map<V, Set<V>> reached, List<V> topologicalList) {
        for (int i = 0; i < topologicalList.size(); i++) {
            V v = topologicalList.get(i);
            // go through all the vertices w that can reach v
            for (E edge : copy.outgoingEdgesOf(v)) {
                V w = Graphs.getOppositeVertex(copy, edge, v);
                if (reached.get(w).contains(v)) {
                    continue;
                } else {
                    Set<V> temp = reached.get(w);
                    // if w can reach vertex v, then w can also reach the vertices v can reach
                    temp.addAll(reached.get(v));
                    temp.add(v);
                    reached.put(w, temp);
                }
            }
        }
    }

    /**
     * This method finds all the vertices w that vertex v can reach.
     * Vertex v keeps track of all the vertices it can reach
     * 
     * @param copy
     * @param reached
     * @param topologicalList
     */
    private void reach(Graph<V, E> copy, Map<V, Set<V>> reached, List<V> topologicalList) {
        for (int i = topologicalList.size() - 1; i > 0; i--) {
            V v = topologicalList.get(i);
            // go through all the vertives w that can reach v
            for (E edge : copy.incomingEdgesOf(v)) {
                V w = Graphs.getOppositeVertex(copy, edge, v);
                if (reached.get(w).contains(v)) {
                    continue;
                } else {
                    Set<V> temp = reached.get(w);
                    // if w can reach vertex v, then w can also reach the vertices v can reach
                    temp.addAll(reached.get(v));
                    temp.add(v);
                    reached.put(w, temp);
                }
            }
        }
    }

    @Override
    public String toString() {
        switch (state) {

            case CentralityState.REACH:
                return "Reach Centrality";
            case CentralityState.DEPENDENCY:
                return "Reach by Centrality (dependency)";
            default:
                return "Default Reach Centrality";
        }
    }

    private enum CentralityState {
        REACH,
        DEPENDENCY
    }
}
