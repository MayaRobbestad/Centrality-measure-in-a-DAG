package no.uib.mayarobbestad.dagcentrality;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.alg.scoring.EigenvectorCentrality;
import org.jgrapht.graph.DefaultEdge;

import no.uib.mayarobbestad.dagcentrality.graph.GraphBuilder;

public class Main {

    static GraphBuilder builder = new GraphBuilder();

    static String directedSyntheticString = "Directed synthetic graphs";
    static String undirectedSynteticString = "Undirected synthetic graphs";

    static ArrayList<String> directedSyntheticData = new ArrayList<>(Arrays.asList(
            "cycle", "path", "star-in", "star-out"));
    static ArrayList<String> undirectedSyntheticData = new ArrayList<>(Arrays.asList(
            "clique-bridge", "clique-four", "clique-three"));

    static Graph[] directedSyntheticGraphs = new Graph[directedSyntheticData.size()];
    static Graph[] undirectedSyntheticGraphs = new Graph[undirectedSyntheticData.size()];

    public static void main(String[] args) throws IOException {
        // printGraphs("data/synthetic/directed/", directedSyntheticData, true);
        // printGraphs("data/synthetic/undirected/", undirectedSyntheticData, false);

        readAndStoreGraphs("data/synthetic/directed/", directedSyntheticData, directedSyntheticGraphs, true);
        readAndStoreGraphs("data/synthetic/undirected/", undirectedSyntheticData, undirectedSyntheticGraphs, false);

        printGraphs(directedSyntheticString, directedSyntheticData, directedSyntheticGraphs);
        printGraphs(undirectedSynteticString, undirectedSyntheticData, undirectedSyntheticGraphs);

        printBetweennessScores(directedSyntheticString, directedSyntheticData, directedSyntheticGraphs);
        printBetweennessScores(undirectedSynteticString, undirectedSyntheticData, undirectedSyntheticGraphs);

        printEigenvectorScores(directedSyntheticString, directedSyntheticData, directedSyntheticGraphs);
        printEigenvectorScores(undirectedSynteticString, undirectedSyntheticData, undirectedSyntheticGraphs);

    }

    /**
     * Given a list of graphs stored as an edge list.
     * The method reads the graphs and stores the graphs in an array of graph
     * objects.
     * 
     * @param rootPath
     * @param input
     * @param graphs
     * @param isDirected
     * @throws IOException
     */
    public static void readAndStoreGraphs(String rootPath, ArrayList<String> input, Graph[] graphs, boolean isDirected)
            throws IOException {
        for (int i = 0; i < input.size(); i++) {
            String graphData = input.get(i);
            String path = rootPath + graphData + ".in";
            graphs[i] = builder.readGraphFromInputFile(path, isDirected);
        }
    }

    public static void printGraphs(String name, ArrayList<String> input, Graph[] graphs) {
        System.out.println("-----" + name + "-----");
        for (int i = 0; i < input.size(); i++) {
            String graphData = input.get(i);
            System.out.println("Graph " + i + ": " + graphData);
            System.out.println(graphs[i]);
        }
        System.out.println("-------------------------------------------------------");
    }

    /**
     * 
     * @param name
     * @param input
     * @param graphs
     */
    private static void printEigenvectorScores(String name, ArrayList<String> input, Graph[] graphs) {
        System.out.println("-----Eigenvector centrality for" + name + "-----");
        for (int i = 0; i < input.size(); i++) {
            String graphData = input.get(i);
            System.out.println("Graph " + i + ": " + graphData);
            System.out.println(new EigenvectorCentrality<>(graphs[i]).getScores());
        }
        System.out.println("-------------------------------------------------------");
    }

    /**
     * Prints out the normalized betweenness centrality score of the vertices in a
     * directed or undirected graph.
     * This betweenness centrality is based on Brandes algorithm.
     * 
     * @param name
     * @param input
     * @param graphs
     */
    private static void printBetweennessScores(String name, ArrayList<String> input, Graph[] graphs) {
        System.out.println("-----" + name + "-----");
        for (int i = 0; i < input.size(); i++) {
            String graphData = input.get(i);
            System.out.println("Graph " + i + ": " + graphData);
            System.out.println(new BetweennessCentrality<>(graphs[i], true).getScores());
        }
        System.out.println("-------------------------------------------------------");
    }
}