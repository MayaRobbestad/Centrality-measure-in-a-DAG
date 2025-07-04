package no.uib.mayarobbestad.dagcentrality;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.alg.scoring.EigenvectorCentrality;
import org.jgrapht.graph.DefaultEdge;

import no.uib.mayarobbestad.dagcentrality.graph.GraphBuilder;
import no.uib.mayarobbestad.dagcentrality.graph.GraphPrinter;

public class Main {

    static GraphBuilder builder = new GraphBuilder();

    static ArrayList<Graph<Integer, DefaultEdge>> graphs = new ArrayList<>();
    static ArrayList<String> graphPaths = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        readAndStoreGraphs("data/dataFiles.txt", graphs, graphPaths, true);
        printGraphs(graphs);
    }

    /**
     * Given a list of graphs stored as ang edge list.
     * The method reads the graphs and stores the graphs in an array of graph
     * objects.
     * 
     * @param file
     * @param graphs
     * @param graphNames
     * @param isDirected
     * @throws IOException
     */
    private static void readAndStoreGraphs(String file, ArrayList<Graph<Integer, DefaultEdge>> graphs,
            ArrayList<String> graphNames,
            boolean isDirected) throws IOException {
        Scanner sc = new Scanner(new FileReader(new File(file)));
        sc.useLocale(Locale.US);
        int n = sc.nextInt();
        sc.nextLine();

        for (int i = 0; i < n; i++) {
            String path = sc.nextLine();
            graphNames.add(i, path);
            graphs.add(i, (Graph<Integer, DefaultEdge>) builder.readGraphFromInputFile(path, isDirected));
        }
    }

    public static void printGraphs(ArrayList<Graph<Integer, DefaultEdge>> graphs) {
        for (int i = 0; i < graphs.size(); i++) {
            System.out.println("----- " + graphPaths.get(i) + " ------");
            System.out.println(graphs.get(i));
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