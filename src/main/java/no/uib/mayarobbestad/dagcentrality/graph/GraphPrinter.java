package no.uib.mayarobbestad.dagcentrality.graph;

import java.util.ArrayList;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.alg.scoring.ClosenessCentrality;
import org.jgrapht.alg.scoring.EigenvectorCentrality;
import org.jgrapht.alg.scoring.HarmonicCentrality;
import org.jgrapht.alg.scoring.KatzCentrality;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultEdge;

import no.uib.mayarobbestad.dagcentrality.algorithms.DegreeCentrality;

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
     * Tried to make a genereic printing method
     * 
     * @param <V>
     * @param <E>
     * @param name
     * @param input
     * @param graphs
     * @param scoring
     */
    public static <V, E> void printCentrality(String name, ArrayList<String> input, Graph<V, E>[] graphs,
            VertexScoringAlgorithm<V, Double> scoring) {
        System.out.println("-----Eigenvector centrality for" + name + "-----");
        for (int i = 0; i < input.size(); i++) {
            String graphData = input.get(i);
            System.out.println("Graph " + i + ": " + graphData);
            // VertexScoringAlgorithm<V, Double> centrality = scoring.apply(graphData);
            System.out.println(scoring.getScores());
        }
        System.out.println("-------------------------------------------------------");
    }

    /**
     * 
     * @param name
     * @param input
     * @param graphs
     */
    public static void printEigenvectorScores(String name, ArrayList<String> input,
            Graph<Integer, DefaultEdge>[] graphs) {
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
    public static void printBetweennessScores(String name, ArrayList<String> input,
            Graph<Integer, DefaultEdge>[] graphs) {
        System.out.println("-----Betweenness centrality for" + name + "-----");
        for (int i = 0; i < input.size(); i++) {
            String graphData = input.get(i);
            System.out.println("Graph " + i + ": " + graphData);
            System.out.println(new BetweennessCentrality<>(graphs[i], true).getScores());
        }
        System.out.println("-------------------------------------------------------");
    }

    /**
     * 
     * @param name
     * @param input
     * @param graphs
     */
    public static void printKatzScores(String name, ArrayList<String> input, Graph<Integer, DefaultEdge>[] graphs) {
        System.out.println("-----Katz centrality for" + name + "-----");
        for (int i = 0; i < input.size(); i++) {
            String graphData = input.get(i);
            System.out.println("Graph " + i + ": " + graphData);
            System.out.println(new KatzCentrality<>(graphs[i]).getScores());
        }
        System.out.println("-------------------------------------------------------");
    }

    /**
     * 
     * @param name
     * @param input
     * @param graphs
     */
    public static void printClosenessScores(String name, ArrayList<String> input,
            Graph<Integer, DefaultEdge>[] graphs) {
        System.out.println("-----Closeness centrality for" + name + "-----");
        for (int i = 0; i < input.size(); i++) {
            String graphData = input.get(i);
            System.out.println("Graph " + i + ": " + graphData);
            System.out.println(new ClosenessCentrality<>(graphs[i]).getScores());
        }
        System.out.println("-------------------------------------------------------");
    }

    /**
     * 
     * @param name
     * @param input
     * @param graphs
     */
    public static void printPageRank(String name, ArrayList<String> input, Graph<Integer, DefaultEdge>[] graphs) {
        System.out.println("-----PageRank for" + name + "-----");
        for (int i = 0; i < input.size(); i++) {
            String graphData = input.get(i);
            System.out.println("Graph " + i + ": " + graphData);
            System.out.println(new PageRank<>(graphs[i]).getScores());
        }
        System.out.println("-------------------------------------------------------");
    }

    /**
     * 
     * @param name
     * @param input
     * @param graphs
     */
    public static void printHarmonmicscores(String name, ArrayList<String> input,
            Graph<Integer, DefaultEdge>[] graphs) {
        System.out.println("-----Harmonic centrality" + name + "-----");
        for (int i = 0; i < input.size(); i++) {
            String graphData = input.get(i);
            System.out.println("Graph " + i + ": " + graphData);
            System.out.println(new HarmonicCentrality<>(graphs[i]).getScores());
        }
        System.out.println("-------------------------------------------------------");
    }

    public static void printDegreescores(String name, ArrayList<String> input,
            Graph<Integer, DefaultEdge>[] graphs) {
        System.out.println("-----Degree centrality" + name + "-----");
        for (int i = 0; i < input.size(); i++) {
            String graphData = input.get(i);
            System.out.println("Graph " + i + ": " + graphData);
            System.out.println(new DegreeCentrality<>(graphs[i], false).getScores());
        }
        System.out.println("-------------------------------------------------------");
    }

}
