package no.uib.mayarobbestad.dagcentrality.graph;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

public class GraphCopy {
    /**
     * returns a simple directed graph copy of the given directed graph
     * 
     * @param g
     * @return
     */
    public static Graph<Integer, DefaultEdge> graphCopy(Graph<Integer, DefaultEdge> g) {
        SimpleDirectedGraph<Integer, DefaultEdge> copy = new SimpleDirectedGraph<>(DefaultEdge.class);
        for (Integer v : g.vertexSet()) {
            copy.addVertex(v);
        }
        for (DefaultEdge e : g.edgeSet()) {
            copy.addEdge(g.getEdgeSource(e), g.getEdgeTarget(e));
        }
        return copy;
    }
}
