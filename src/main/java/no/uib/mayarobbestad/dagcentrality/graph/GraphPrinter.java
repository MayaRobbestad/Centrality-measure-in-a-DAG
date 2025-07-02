package no.uib.mayarobbestad.dagcentrality.graph;

import java.util.ArrayList;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.alg.scoring.EigenvectorCentrality;

public class GraphPrinter {

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
    public static void printEigenvectorScores(String name, ArrayList<String> input, Graph[] graphs) {
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
    public static void printBetweennessScores(String name, ArrayList<String> input, Graph[] graphs) {
        System.out.println("-----" + name + "-----");
        for (int i = 0; i < input.size(); i++) {
            String graphData = input.get(i);
            System.out.println("Graph " + i + ": " + graphData);
            System.out.println(new BetweennessCentrality<>(graphs[i], true).getScores());
        }
        System.out.println("-------------------------------------------------------");
    }

}
