package no.uib.mayarobbestad.dagcentrality;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.alg.scoring.EigenvectorCentrality;
import org.jgrapht.graph.DefaultEdge;

import no.uib.mayarobbestad.dagcentrality.graph.GraphBuilder;
import no.uib.mayarobbestad.dagcentrality.graph.GraphPrinter;

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

        GraphPrinter.printGraphs(directedSyntheticString, directedSyntheticData, directedSyntheticGraphs);
        GraphPrinter.printGraphs(undirectedSynteticString, undirectedSyntheticData, undirectedSyntheticGraphs);

        GraphPrinter.printBetweennessScores(directedSyntheticString, directedSyntheticData, directedSyntheticGraphs);
        GraphPrinter.printBetweennessScores(undirectedSynteticString, undirectedSyntheticData,
                undirectedSyntheticGraphs);

        GraphPrinter.printEigenvectorScores(directedSyntheticString, directedSyntheticData, directedSyntheticGraphs);
        GraphPrinter.printEigenvectorScores(undirectedSynteticString, undirectedSyntheticData,
                undirectedSyntheticGraphs);

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

}