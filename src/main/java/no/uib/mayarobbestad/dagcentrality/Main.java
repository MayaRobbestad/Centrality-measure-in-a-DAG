package no.uib.mayarobbestad.dagcentrality;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

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
            "cycle", "path", "star-in", "star-out", "twoRootedTreeWithCycle"));
    static ArrayList<String> undirectedSyntheticData = new ArrayList<>(Arrays.asList(
            "clique-bridge", "clique-four", "clique-three"));

    static Graph<Integer, DefaultEdge>[] directedSyntheticGraphs = new Graph[directedSyntheticData.size()];
    static Graph<Integer, DefaultEdge>[] undirectedSyntheticGraphs = new Graph[undirectedSyntheticData.size()];

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

        for (int i = 0; i < directedSyntheticData.size(); i++) {
            System.out.println(graphHasCycle(directedSyntheticGraphs[i]));
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
    public static <V, E> void readAndStoreGraphs(String rootPath, ArrayList<String> input,
            Graph<Integer, DefaultEdge>[] graphs, boolean isDirected)
            throws IOException {
        for (int i = 0; i < input.size(); i++) {
            String graphData = input.get(i);
            String path = rootPath + graphData + ".in";
            graphs[i] = (Graph<Integer, DefaultEdge>) builder.readGraphFromInputFile(path, isDirected);
        }
    }

    /**
     * This algoritm checks if there is a cycle in the directed graph,
     * by performing imperative dfs from each root node
     * 
     * @param <V>
     * @param <E>
     * @param g
     * @return
     */
    public static <V, E> boolean graphHasCycle(Graph<Integer, DefaultEdge> g) {
        boolean[] globalVisited = new boolean[g.vertexSet().size()];
        HashSet<Integer> roots = findAllRootVertices(g);
        // set of vertices visited in the current recusrive call
        Stack<Integer> toSearch = new Stack<>();
        if (roots.isEmpty()) {
            return true;
        }
        for (Integer root : roots) {
            toSearch.push(root);

            boolean[] localVisited = new boolean[g.vertexSet().size()];
            while (!toSearch.isEmpty()) {
                Integer current = toSearch.pop();
                globalVisited[current] = true;
                localVisited[current] = true;
                for (DefaultEdge neighbourEdge : g.outgoingEdgesOf(current)) {
                    Integer neighbour = g.getEdgeTarget(neighbourEdge);
                    if (localVisited[neighbour]) {
                        return true;
                    }
                    if (!globalVisited[neighbour] && !localVisited[neighbour]) {
                        // only continue the recursive call if the vertex has not been
                        // visited in this tree, or if it has not been visited by any other
                        // tree with any other root
                        toSearch.push(neighbour);
                    }
                }
            }
        }
        return false;
    }

    private static <V, E> HashSet<Integer> findAllRootVertices(Graph<Integer, DefaultEdge> g) {
        HashSet<Integer> roots = new HashSet<>();
        for (Integer vertex : g.vertexSet()) {
            if (g.inDegreeOf(vertex) == 0) {
                roots.add(vertex);
            }
        }
        return roots;
    }

    /**
     * Remove edges such that the graph becomes a DAG.
     * To find the minimum set of edges to remove, is called minimum feedback arc
     * set,
     * this problem is NP-hard, therefore this method is a greedy approach for now.
     */
    public static void removeCycleFromDirectedGraph() {
        HashSet<DefaultEdge> toRemove = new HashSet<>();

    }

}