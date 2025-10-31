package no.uib.mayarobbestad.dagcentrality.algorithms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;

/**
 * Degree centrality
 * 
 * Calculates the degree centrality of each vertex of a graph. The degree
 * centrality of a
 * vertex $v$ is given by $C_D=deg(v)$
 * 
 * The algorithm is based on the description of Freeman in this article:
 * Freeman, L. C. (1978). Centrality in social networks conceptual
 * clarification. Social networks, 1(3), 215-239.
 * 
 * For the directed grahs, we distinguish between indegree and outdegree, which
 * has been stated here:
 * Koschützki, D., Lehmann, K. A., Peeters, L., Richter, S., Tenfelde-Podehl,
 * D., & Zlotowski, O. (2005).
 * Centrality indices. In Network analysis: methodological foundations (pp.
 * 16-61). Berlin, Heidelberg: Springer Berlin Heidelberg.
 * 
 * Looked at how JgraphT implements VertexScoring algorithms for the other
 * centrality measures, such as betweenness, closeness etc.
 */
public class DegreeCentrality<V, E> implements VertexScoringAlgorithm<V, Double> {

    private final Graph<V, E> graph;
    private Map<V, Double> scores;
    private CentralityState state;
    private boolean normalize;

    /**
     * When this class is called through this constructor, the centrality measure
     * is
     * calculated based
     * on the total number of degrees a vertex has. For directed graphs both the
     * indegree and outdegree are
     * summed up
     * 
     * @param graph     the graph on which the centrality measure should be
     *                  calculated
     * @param normalize true if the centrality score should be normalized by
     *                  dividing the number of neighbours by n-1
     *                  false if the centrality score should be the total number of
     *                  neighbours (based on all, indegree or outdegree)
     */
    public DegreeCentrality(Graph<V, E> graph, boolean normalize) {
        this.graph = graph;
        this.state = CentralityState.ALL;
        this.normalize = normalize;
    }

    /**
     * By calling this constructor it is possible to distinguish between indegree
     * centrality, and outdegree centrality.
     * When only incomming edges should be accounted for, then set indegree to true
     * When only outgoing edges should be accounted for, then set indegree to false
     * 
     * @param graph     the graph on which the centrality measure should be
     *                  calculated
     * @param indegree  true if the centrality measure should only take the indegree
     *                  of a vertex v into account
     *                  false if the centrality measure should only take the
     *                  outdegree of a vertex v into account
     * @param normalize true if the centrality score should be normalized by
     *                  dividing the number of neighbours by n-1
     *                  false if the centrality score should be the total number of
     *                  neighbours (based on all, indegree or outdegree)
     */
    public DegreeCentrality(Graph<V, E> graph, boolean indegree, boolean normalize) {
        this.graph = graph;
        if (indegree) {
            this.state = CentralityState.INDEGREE;
        } else {
            this.state = CentralityState.OUTDEGREE;
        }
        this.normalize = normalize;
    }

    @Override
    public Map<V, Double> getScores() {
        if (scores == null) {
            run();
        }
        return Collections.unmodifiableMap(scores);

    }

    /**
     * Compute the degree centrality measure for all vertices in the graph
     */
    private void run() {
        scores = new HashMap<>();
        for (V v : graph.vertexSet()) {
            scores.put(v, 0.0);
        }
        for (V v : graph.vertexSet()) {
            double score = compute(v);
            if (normalize) {
                score /= graph.vertexSet().size() - 1;
            }
            scores.put(v, score);
        }
    }

    /**
     * Computes the number of neighbours a vertex has.
     * When a graph is directed, it either returns the number of number of outgoing
     * edges or the number of ingoing edges.
     * 
     * @param v
     */
    private double compute(V v) {
        double numNeighbours = 0.0;
        switch (state) {
            case CentralityState.ALL:
                numNeighbours = graph.degreeOf(v);
                break;
            case CentralityState.INDEGREE:
                numNeighbours = graph.inDegreeOf(v);
                break;
            case CentralityState.OUTDEGREE:
                numNeighbours = graph.outDegreeOf(v);
                break;
            default:
                break;
        }
        return numNeighbours;

    }

    @Override
    public Double getVertexScore(V v) {
        if (!graph.containsVertex(v)) {
            throw new IllegalArgumentException("Cannot return score of unknown vertex");
        }
        if (scores == null) {
            run();
        }
        return scores.get(v);
    }

    @Override
    public String toString() {
        switch (state) {
            case CentralityState.ALL:
                return "Degree Centrality";
            case CentralityState.INDEGREE:
                return "In-Degree Centrality";
            case CentralityState.OUTDEGREE:
                return "Out-Degree Centrality";
            default:
                return "Default Degree Centrality";
        }
    }

    private enum CentralityState {
        ALL,
        INDEGREE,
        OUTDEGREE
    }
}
