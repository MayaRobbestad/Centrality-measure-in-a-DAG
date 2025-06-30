package no.uib.mayarobbestad.dagcentrality;

import no.uib.mayarobbestad.dagcentrality.graph.GraphBuilder;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.gml.GmlImporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    static GraphBuilder builder = new GraphBuilder();

    static int numberOfBlockchainGraphs = 2;
    static int numberOfCompanyGraphs = 0;
    static int numberOfSoftwareGraphs = 3;
    static int numberOfDirectedSyntheticGraphs = 4;
    static int numberOfUndirectedSyntheticGraphs = 3;

    static ArrayList<String> directedSyntheticGraphs = new ArrayList<>(Arrays.asList(
            "cycle", "path", "star-in", "star-out"));
    static ArrayList<String> undirectedSyntheticGraphs = new ArrayList<>(Arrays.asList(
            "clique-bridge", "clique-four", "clique-three"));

    public static void main(String[] args) throws IOException {
        printGraphs("data/synthetic/directed/", directedSyntheticGraphs);
        printGraphs("data/synthetic/undirected/", undirectedSyntheticGraphs);

    }

    /**
     * Prints graphs formated as an edgelist
     * 
     * @param rootPath
     * @param graphs
     */
    public static void printGraphs(String rootPath, ArrayList<String> graphs) throws IOException {
        System.out.println("-----Graphs from " + rootPath + "-----");
        for (int i = 0; i < graphs.size(); i++) {
            String graphData = graphs.get(i);
            String path = rootPath + graphData + ".in";
            System.out.println("Graph " + i + ": " + graphData);
            System.out.println(builder.readGraphFromInputFile(path, true));
        }
        System.out.println("-------------------------------------------------------");

    }
}