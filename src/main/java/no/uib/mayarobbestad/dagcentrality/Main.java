package no.uib.mayarobbestad.dagcentrality;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import no.uib.mayarobbestad.dagcentrality.algorithms.GreedyFAS;
import no.uib.mayarobbestad.dagcentrality.graph.GraphBuilder;
import no.uib.mayarobbestad.dagcentrality.graph.GraphCopy;

public class Main {

    static GraphBuilder builder = new GraphBuilder();

    static ArrayList<Graph<Integer, DefaultEdge>> graphs = new ArrayList<>();
    static ArrayList<String> graphPaths = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        readAndStoreGraphs("data/dataFiles.txt", graphs, graphPaths, true);
        printGraphs(graphs);

        ArrayList<Graph<Integer, DefaultEdge>> copies = new ArrayList<>();
        for (Graph<Integer, DefaultEdge> graph : graphs) {
            copies.add(GraphCopy.graphCopy(graph));
        }
        printGraphs(copies);

        int graphNum = 0;
        List<Integer> gr = GreedyFAS.GR(graphs.get(graphNum));
        int n = graphs.get(graphNum).vertexSet().size();
        System.out.println("n: " + n);
        System.out.println(gr.size());
        for (int i = 0; i < n; i++) {
            System.out.println("index: " + i + " vertex: " + gr.get(i));
        }
        for (DefaultEdge edge : GreedyFAS.removeCycleFromDirectedGraph(graphs.get(graphNum))) {
            System.out.println(edge);
        }
        List<Graph<Integer, DefaultEdge>> test = new ArrayList<>();
        test.add(graphs.get(graphNum));
        printGraphs(test);
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

    public static void printGraphs(List<Graph<Integer, DefaultEdge>> graphs) {
        for (int i = 0; i < graphs.size(); i++) {
            System.out.println("----- " + graphPaths.get(i) + " ------");
            System.out.println(graphs.get(i));
        }

    }
}