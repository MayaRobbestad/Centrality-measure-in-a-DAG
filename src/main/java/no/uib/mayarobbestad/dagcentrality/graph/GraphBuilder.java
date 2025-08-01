package no.uib.mayarobbestad.dagcentrality.graph;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

/**
 * This class reads a graph from a file and stores the data in a JGraphT graph
 * datastructure,
 * took inspiration from INF102 semester assignment 2
 */
public class GraphBuilder {

    /**
     * Reads a simple graph from an edgelist file, where all vertices are
     * represented as Integers.
     *
     * @param file
     * @return
     */
    public Graph<Integer, DefaultEdge> readGraphFromInputFile(String file, boolean directed) throws IOException {
        Scanner sc = new Scanner(new FileReader(new File(file)));
        sc.useLocale(Locale.US);
        int n = sc.nextInt(), m = sc.nextInt();
        Graph<Integer, DefaultEdge> g;
        if (directed) {
            g = new SimpleDirectedGraph<>(DefaultEdge.class);
        } else {
            g = new SimpleGraph<>(DefaultEdge.class);
        }

        for (int i = 0; i < m; i++) {
            int u = sc.nextInt(), v = sc.nextInt();
            if (!g.containsVertex(v)) {
                g.addVertex(v);
            }
            if (!g.containsVertex(u)) {
                g.addVertex(u);
            }
            g.addEdge(u, v);
        }
        return g;
    }

    public Graph<Integer, DefaultEdge> readGraphFromGmlFile(String file, boolean directed) {
        return null;
    }

}
