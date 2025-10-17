package no.uib.mayarobbestad.dagcentrality.algorithms;

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
import org.jgrapht.traverse.TopologicalOrderIterator;

/**
 * This centrality calculates the score of a vertex based on the number of paths
 * a vertex is on.
 * Inspired by betweenness centrality, but instead of finding the shortest paths
 * from every vertex to
 * all other vertices, we calculate the score based on the number of shortest
 * paths from all sources to sinks
 */
public class SSSPPBetweenness<V, E> implements VertexScoringAlgorithm<V, Long> {

    private Graph<V, E> graph;
    private Map<V, Long> scores;
    private Map<V, Map<V, Long>> dist;
    private Set<V> sources;
    private Set<V> internal;
    private Set<V> sinks;

    public SSSPPBetweenness(Graph<V, E> graph) {
        this.graph = graph;
        this.scores = new HashMap<>();
        this.dist = new HashMap<>();
        this.sources = new HashSet<>();
        this.internal = new HashSet<>();
        this.sinks = new HashSet<>();
        run();
    }

    @Override
    public Map<V, Long> getScores() {
        if (scores == null) {
            run();
        }
        return Collections.unmodifiableMap(scores);
    }

    @Override
    public Long getVertexScore(V v) {
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
            scores.put(current, (long) 0);
        }

        initializeDP();

        spFromSources(topologicalList);
        spFromSinks(topologicalList);
        System.out.println("distance:" + dist);
    }

    private void APSPNotNormalized(Set<V> sources, Set<V> internal, Set<V> sinks) {
        for (V s : sources) {
            for (V t : sinks) {
                if (!dist.get(s).containsKey(t)) // t not reachable from s
                    continue;
                for (V v : internal) {
                    if (!dist.get(s).containsKey(v) || !dist.get(v).containsKey(t))
                        continue;
                    if (dist.get(s).get(v) + dist.get(v).get(t) == dist.get(s).get(t))

                        scores.put(v, scores.get(v) + 1);
                }
            }
        }
    }

    private void spFromSinks(List<V> topologicalList) {
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

}
