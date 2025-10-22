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
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

/**
 * This centrality calculates the score of a vertex based on the number of paths
 * a vertex is on.
 * Inspired by betweenness centrality, but instead of finding the shortest paths
 * from every vertex to
 * all other vertices, we calculate the score based on the number of shortest
 * paths from all sources to sinks
 */
public class SAASBetweenness<V, E> implements VertexScoringAlgorithm<V, BigDecimal> {

    private Graph<V, E> graph;
    private Map<V, BigDecimal> scores;
    private Map<V, Map<V, Long>> dist;
    private Set<V> sources;
    private Set<V> internal;
    private Set<V> sinks;
    private Map<V, Map<V, Long>> sigma;

    public SAASBetweenness(Graph<V, E> graph) {
        this.graph = graph;
        this.scores = new HashMap<>();
        this.dist = new HashMap<>();
        this.sources = new HashSet<>();
        this.internal = new HashSet<>();
        this.sinks = new HashSet<>();
        this.sigma = new HashMap<>();
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
        if (scores == null) {
            run();
        }
        return getScores().get(v);
    }

    /**
     * The actual implementation of the algorithm
     */
    private void run() {

        List<V> topologicalList = new ArrayList<>(graph.vertexSet().size());
        TopologicalOrderIterator<V, E> iterator = new TopologicalOrderIterator<>(graph);

        while (iterator.hasNext()) {
            V current = iterator.next();
            topologicalList.add(current);
            // initialize source, internal and sink
            if (graph.inDegreeOf(current) == 0) { // source
                this.sources.add(current);
            } else if (graph.outDegreeOf(current) == 0) { // sink
                this.sinks.add(current);
            } else {
                this.internal.add(current); // internal
            }
            // all vertices start with score 0
            scores.put(current, new BigDecimal("0"));
        }

        initializeDP();

        spFromSources(topologicalList);
        spFromSinks(topologicalList);
        findSigma(topologicalList);
        SAASCentrality();

        // spFromSinksWithOptimization(topologicalList);
        // spFromSourcesAndSinksAndFindSigma(topologicalList);

    }

    private void findSigma(List<V> topoList) {
        for (V s : this.sources) {
            Map<V, Long> distanceFromStoAll = new HashMap<>();
            for (V v : graph.vertexSet()) {
                if (v.equals(s)) {
                    distanceFromStoAll.put(v, (long) 1);
                } else {
                    distanceFromStoAll.put(v, (long) 0);
                }
            }
            sigma.put(s, distanceFromStoAll);
        }

        for (V s : this.sources) {
            for (int i = 0; i < topoList.size(); i++) {
                V v = topoList.get(i);
                long numSP = sigma.get(s).get(v);
                for (E e : graph.incomingEdgesOf(v)) {
                    V w = Graphs.getOppositeVertex(graph, e, v);
                    if (this.dist.get(s).get(w) + 1 == this.dist.get(s).get(v)) {
                        numSP += sigma.get(s).get(w);
                    }
                }
                sigma.get(s).put(v, numSP);
            }
        }

    }

    /**
     * The actual implementation of SAAS-Betweenness
     * This method is not normalized
     * 
     * @param sources
     * @param internal
     * @param sinks
     */
    private void SAASCentrality() {
        for (V s : this.sources) {
            for (V t : this.sinks) {
                if (!dist.get(s).containsKey(t)) // t not reachable from s
                    continue;
                for (V v : this.internal) {
                    if (!dist.get(s).containsKey(v) || !dist.get(v).containsKey(t))
                        continue;
                    if (dist.get(s).get(v) + dist.get(v).get(t) == dist.get(s).get(t))
                        scores.put(v, scores.get(v).add(
                                BigDecimal.ONE.divide(new BigDecimal(sigma.get(s).get(t)), 5, RoundingMode.HALF_UP)));
                }
            }
        }
    }

    private void spFromSinks(List<V> topologicalList) {
        // could perhaps stop when we have reached a source vertex, since we already
        // have computed that
        for (int i = topologicalList.size() - 1; i > 0; i--) {
            V v = topologicalList.get(i);
            for (V t : sinks) {
                for (E e : graph.incomingEdgesOf(v)) {
                    V u = Graphs.getOppositeVertex(graph, e, v);
                    if (dist.get(v).get(t) < dist.get(u).get(t)) {
                        // dist(s,u) = dist(s,v) + 1
                        dist.get(u).put(t, dist.get(v).get(t) + 1);
                    }
                }
            }
        }
    }

    private void spFromSinksWithOptimization(List<V> topologicalList) {
        // could perhaps stop when we have reached a source vertex, since we already
        // have computed that
        for (int i = topologicalList.size() - 1; i > 0; i--) {
            V v = topologicalList.get(i);
            if (this.sources.contains(v)) {
                // skip computing shortest path from source to sink again
                continue;
            }
            for (V t : sinks) {
                for (E e : graph.incomingEdgesOf(v)) {
                    V u = Graphs.getOppositeVertex(graph, e, v);
                    if (dist.get(v).get(t) < dist.get(u).get(t)) {
                        // dist(s,u) = dist(s,v) + 1
                        dist.get(u).put(t, dist.get(v).get(t) + 1);
                    }
                }
            }
        }
    }

    private void spFromSources(List<V> topologicalList) {
        for (int i = 0; i < topologicalList.size(); i++) {
            V v = topologicalList.get(i);
            for (V s : sources) {
                for (E e : graph.outgoingEdgesOf(v)) {
                    V u = Graphs.getOppositeVertex(graph, e, v);
                    if (dist.get(s).get(v) < dist.get(s).get(u)) {
                        // dist(s,u) = dist(s,v) + 1
                        dist.get(s).put(u, dist.get(s).get(v) + 1);
                    }
                }
            }
        }
    }

    private void initializeDP() {

        for (V s : this.sources) {
            Map<V, Long> sourceToAll = new HashMap<>();
            for (V v : graph.vertexSet()) {
                // set dist(s,s) to 0
                if (v.equals(s)) {
                    sourceToAll.put(v, (long) 0);
                } else {
                    // set everything to big number (basically infinity)
                    sourceToAll.put(v, Long.MAX_VALUE);
                }
            }
            this.dist.put(s, sourceToAll);
        }
        for (V v : this.internal) {
            Map<V, Long> internalToSink = new HashMap<>();
            for (V t : this.sinks) {
                internalToSink.put(t, Long.MAX_VALUE);
            }
            dist.put(v, internalToSink);
        }
        for (V v : this.sinks) {
            Map<V, Long> sinkToSink = new HashMap<>();
            for (V t : this.sinks) {
                if (v.equals(t)) {
                    sinkToSink.put(t, (long) 0);
                } else {
                    sinkToSink.put(t, Long.MAX_VALUE);
                }
            }
            dist.put(v, sinkToSink);
        }

    }

    /*
     * private void spFromSourcesAndSinksAndFindSigma(List<V> topologicalList) {
     * 
     * for (V s : this.sources) {
     * Map<V, Integer> distanceFromStoAll = new HashMap<>();
     * for (V v : graph.vertexSet()) {
     * if (v.equals(s)) {
     * distanceFromStoAll.put(v, 1);
     * } else {
     * distanceFromStoAll.put(v, 0);
     * }
     * }
     * sigma.put(s, distanceFromStoAll);
     * }
     * 
     * System.out.println(topologicalList);
     * for (int i = 0; i < topologicalList.size(); i++) {
     * V v = topologicalList.get(i);
     * for (V s : sources) {
     * for (E e : graph.outgoingEdgesOf(v)) {
     * V u = Graphs.getOppositeVertex(graph, e, v);
     * 
     * }
     * }
     * 
     * }
     * System.out.println("sigma:" + this.sigma);
     * 
     * for (int i = topologicalList.size() - 1; i > 0; i--) {
     * V v = topologicalList.get(i);
     * if (this.sources.contains(v)) {
     * // skip computing shortest path from source to sink again
     * continue;
     * }
     * for (V t : sinks) {
     * for (E e : graph.incomingEdgesOf(v)) {
     * V u = Graphs.getOppositeVertex(graph, e, v);
     * if (dist.get(v).get(t) < dist.get(u).get(t)) {
     * // dist(s,u) = dist(s,v) + 1
     * dist.get(u).put(t, dist.get(v).get(t) + 1);
     * }
     * }
     * }
     * }
     * }
     */
}
