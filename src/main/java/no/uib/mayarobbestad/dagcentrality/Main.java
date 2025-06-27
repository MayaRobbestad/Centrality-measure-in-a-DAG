package no.uib.mayarobbestad.dagcentrality;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.gml.GmlImporter;

public class Main {
    public static void main(String[] args) {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        g.addVertex("v1");
        g.addVertex("v2");
        g.addVertex("v3");
        g.addEdge("v1", "v2");
        g.addEdge("v2", "v3");
        System.out.println(g);
    }
}