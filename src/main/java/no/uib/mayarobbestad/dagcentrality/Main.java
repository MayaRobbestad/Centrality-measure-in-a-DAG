package no.uib.mayarobbestad.dagcentrality;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.alg.scoring.EigenvectorCentrality;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.specifics.DirectedSpecifics;

import no.uib.mayarobbestad.dagcentrality.graph.GraphBuilder;
import no.uib.mayarobbestad.dagcentrality.graph.GraphPrinter;

public class Main {

    static GraphBuilder builder = new GraphBuilder();

    static ArrayList<Graph<Integer, DefaultEdge>> graphs = new ArrayList<>();
    static ArrayList<String> graphPaths = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        readAndStoreGraphs("data/dataFiles.txt", graphs, graphPaths, true);
        printGraphs(graphs);

        ArrayList<Graph<Integer, DefaultEdge>> copies = new ArrayList<>();
        for (Graph<Integer, DefaultEdge> graph : graphs) {
            copies.add(graphCopy(graph));
        }
        printGraphs(copies);

        int graphNum = 2;
        List<Integer> gr = GR(graphs.get(graphNum));
        int n = graphs.get(graphNum).vertexSet().size();
        System.out.println("n: " + n);
        System.out.println(gr.size());
        for (int i = 0; i < n; i++) {
            System.out.println("index: " + i + " vertex: " + gr.get(i));
        }

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

    /**
     * This algorithm finds a vertex sequence in a directed graph such that
     * the number of backwards directing edges is minimized. These edges are aprt
     * of the minimimal Feedback Arc Set
     */
    public static List<Integer> GR(Graph<Integer, DefaultEdge> g) {
        List<Integer> s1 = new ArrayList<>();
        List<Integer> s2 = new ArrayList<>();
        List<Integer> s = new ArrayList<>();

        Graph<Integer, DefaultEdge> copy = graphCopy(g);

        Set<Integer> sinks = new HashSet<>();
        Set<Integer> sources = new HashSet<>();

        for (Integer v : copy.vertexSet()) {
            if (copy.inDegreeOf(v) == 0) {
                sources.add(v);
            }
            if (copy.outDegreeOf(v) == 0) {
                sinks.add(v);
            }
        }
        for (Integer sink : sinks) {
            System.out.println("sink: " + sink);
        }

        for (Integer source : sources) {
            System.out.println("source: " + source);

        }

        while (!copy.vertexSet().isEmpty()) {

            while (!sinks.isEmpty()) {
                System.out.println("sinks");
                Integer v = sinks.iterator().next();
                sinks.remove(v);
                // if a sink vertex also has become a source vertex, remove this vertex from the
                // source list
                if (copy.inDegreeOf(v) == 0) {
                    sources.remove(v);
                }
                System.out.println("v: " + v);
                // list is reversed later
                s2.add(v);
                Set<DefaultEdge> incomingEdges = new HashSet<>(copy.incomingEdgesOf(v));
                System.out.println("number of incoming edges: " + incomingEdges.size());
                for (DefaultEdge edge : incomingEdges) {
                    System.out.println("edge" + edge);
                    Integer u = copy.getEdgeSource(edge);
                    copy.removeEdge(edge);
                    // check if u is now a sink vertex
                    if (copy.outDegreeOf(u) == 0) {
                        sinks.add(u);
                    }
                }
                copy.removeVertex(v);
            }

            while (!sources.isEmpty()) {
                System.out.println("sources");
                Integer v = sources.iterator().next();
                sources.remove(v);
                s1.add(v);
                Set<DefaultEdge> outgoingEdges = new HashSet<>(copy.outgoingEdgesOf(v));
                for (DefaultEdge edge : outgoingEdges) {
                    Integer u = copy.getEdgeTarget(edge);
                    copy.removeEdge(edge);
                    // check if u is now a source vertex
                    if (copy.inDegreeOf(u) == 0) {
                        sources.add(u);
                    }
                }
                copy.removeVertex(v);
            }

            // delta(u) = outdegree - indegree
            if (!copy.vertexSet().isEmpty()) {
                int max = Integer.MIN_VALUE;
                Integer bestVertex = 0;
                for (Integer v : copy.vertexSet()) {
                    int delta = copy.outDegreeOf(v) - copy.inDegreeOf(v);
                    if (delta > max) {
                        max = delta;
                        bestVertex = v;
                    }
                }
                s1.add(bestVertex);

                // update in and outdegrees
                // TODO: duplicate code
                Set<DefaultEdge> incomingEdges = new HashSet<>(copy.incomingEdgesOf(bestVertex));
                for (DefaultEdge edge : incomingEdges) {
                    Integer u = copy.getEdgeSource(edge);
                    copy.removeEdge(edge);
                    // check if u is now a sink vertex
                    if (copy.outDegreeOf(u) == 0) {
                        sinks.add(u);
                    }
                }

                Set<DefaultEdge> outgoingEdges = new HashSet<>(copy.outgoingEdgesOf(bestVertex));
                for (DefaultEdge edge : outgoingEdges) {
                    Integer u = copy.getEdgeTarget(edge);
                    copy.removeEdge(edge);
                    // check if u is now a source vertex
                    if (copy.inDegreeOf(u) == 0) {
                        sources.add(u);
                    }
                }
                copy.removeVertex(bestVertex);
            }
        }

        int x = s1.size();
        Collections.reverse(s2);
        System.out.println("s1 size: " + x);
        System.out.println("s2 size: " + s2.size());

        for (int i = 0; i < g.vertexSet().size(); i++) {
            System.out.println("x: " + x);
            if (i < x) {
                System.out.println("i: " + i + "elem: " + s1.get(i));
                s.add(s1.get(i));
            } else {
                System.out.println("i: " + i + " elem: " + s2.get(i - x));
                s.add(s2.get(i - x));
            }
        }
        System.out.println("s size: " + s.size());

        return s;
    }

    /**
     * returns a simple directed graph copy of the given directed graph
     * 
     * @param g
     * @return
     */
    private static Graph<Integer, DefaultEdge> graphCopy(Graph<Integer, DefaultEdge> g) {
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