package no.uib.mayarobbestad.dagcentrality;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.IDN;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.alg.scoring.EigenvectorCentrality;
import org.jgrapht.alg.scoring.HarmonicCentrality;
import org.jgrapht.alg.scoring.KatzCentrality;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.gml.GmlImporter;
import org.jgrapht.util.SupplierUtil;

import no.uib.mayarobbestad.dagcentrality.algorithms.DegreeCentrality;
import no.uib.mayarobbestad.dagcentrality.graph.GraphBuilder;

public class Main {

    // builds the graph
    static GraphBuilder builder = new GraphBuilder();

    // The graphs to be run
    static ArrayList<Graph<Integer, DefaultEdge>> graphs = new ArrayList<>();
    static ArrayList<String> graphDirectory = new ArrayList<>();

    // The centrality algorithms to be run
    static final boolean DEGREE = true;
    static final boolean INDEGREE = true;
    static final boolean OUTDEGREE = true;
    static final boolean CLOSENESS = true;
    static final boolean INHARMONIC = true; // variation of closeness
    static final boolean OUTHARMONIC = true;
    static final boolean BETWEENNESS = true;
    static final boolean EIGENVECTOR = true;
    static final boolean KATZ = true;
    static final boolean PAGERANK = true;
    static final boolean MYDAGCENTRALITY = false;

    public static void main(String[] args) throws IOException {
        readAndStoreInputGraphs("data/dataFiles.txt", graphs, graphDirectory, true);
        storeCentralityScoresInFile("results/results.txt", graphs);
        storeCentralityScoresInChart("results/charts", graphs);
        // chartExample();
        // multipleBarChartExample();

    }

    private static void storeCentralityScoresInChart(String string, ArrayList<Graph<Integer, DefaultEdge>> graphs)
            throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        DefaultCategoryDataset[] datasets = new DefaultCategoryDataset[graphs.size()];
        if (INDEGREE) {
            // LIST OF GRAPHS
            // number of double bars
            VertexScoringAlgorithm<Integer, Double> algorithm;
            for (int i = 0; i < graphs.size(); i++) {
                algorithm = new DegreeCentrality<>(graphs.get(i), true, true);
                Map<Integer, Double> scores = algorithm.getScores();
                for (Integer v : scores.keySet()) {
                    dataset.addValue(scores.get(v), graphDirectory.get(i), v);
                }
            }

            JFreeChart barChart = ChartFactory.createBarChart("In-degree", "Graphs", "Centrality score",
                    dataset, PlotOrientation.VERTICAL, true, true, false);

            ChartUtils.saveChartAsPNG(new File("results/charts/centrality-charts-indegree.png"), barChart, 650, 400);
        }

        if (OUTDEGREE) {
            // LIST OF GRAPHS
            // number of double bars
            VertexScoringAlgorithm<Integer, Double> algorithm;
            for (int i = 0; i < graphs.size(); i++) {
                algorithm = new DegreeCentrality<>(graphs.get(i), false, true);
                Map<Integer, Double> scores = algorithm.getScores();
                for (Integer v : scores.keySet()) {
                    dataset.addValue(scores.get(v), graphDirectory.get(i), v);
                }
            }

            JFreeChart barChart = ChartFactory.createBarChart("Out-degree", "Graphs", "Centrality score",
                    dataset, PlotOrientation.VERTICAL, true, true, false);

            ChartUtils.saveChartAsPNG(new File("results/charts/centrality-charts-outdegree.png"), barChart, 650, 400);
        }

    }

    private static void multipleBarChartExample() throws IOException {
        String P1 = "Player 1";
        String P2 = "Player 2";
        String Attk = "Attack";
        String Def = "Defense";
        String Speed = "Speed";
        String Stealth = "Stealth";

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Player 1
        dataset.addValue(10, P1, Attk);
        dataset.addValue(7, P1, Def);
        dataset.addValue(6, P1, Speed);
        dataset.addValue(6, P1, Stealth);

        // Player 2
        dataset.addValue(7, P2, Attk);
        dataset.addValue(9, P2, Def);
        dataset.addValue(8, P2, Speed);
        dataset.addValue(8, P2, Stealth);

        JFreeChart barChart = ChartFactory.createBarChart("JFreeChart BarChart", "Players", "Points",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        ChartUtils.saveChartAsPNG(new File("results/charts/chartExample.png"), barChart, 650, 400);

    }

    /**
     * Code from https://www.baeldung.com/jfreechart-visualize-data
     */
    private static void chartExample() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(200, "Sales", "January");
        dataset.addValue(150, "Sales", "February");
        dataset.addValue(180, "Sales", "March");
        dataset.addValue(260, "Sales", "April");
        dataset.addValue(300, "Sales", "May");

        JFreeChart chart = ChartFactory.createBarChart(
                "Monthly Sales",
                "Month",
                "Sales",
                dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setContentPane(chartPanel);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }

    /**
     * Runs the algorithms that are set to true. These algorithms are stated as
     * field variables.
     * The results of the centrality measures are then stored in the file given
     * 
     * @param string
     * @throws IOException
     */
    private static void storeCentralityScoresInFile(String file, ArrayList<Graph<Integer, DefaultEdge>> graphs)
            throws IOException {
        FileWriter writer = new FileWriter(file);
        int numGraphs = graphs.size();
        VertexScoringAlgorithm<Integer, Double> centralityAlgorithm;
        if (DEGREE) {
            writer.write("Degree centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                centralityAlgorithm = new DegreeCentrality<>(graphs.get(i), true);
                // makes sure that all vertices are written in the file, eventhopugh we might
                // have deleted some vertieces previously
                StringBuilder builder = new StringBuilder();
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(v + ":" + centralityAlgorithm.getVertexScore(v) + ",");
                }
                int n = builder.length();
                builder.deleteCharAt(n - 1);
                writer.write(builder.toString());
                writer.write("]\n");
            }
            writer.write("\n");
        }
        if (INDEGREE) {
            writer.write("Indegree centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                centralityAlgorithm = new DegreeCentrality<>(graphs.get(i), true, true);
                StringBuilder builder = new StringBuilder();
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(v + ":" + centralityAlgorithm.getVertexScore(v) + ",");
                }
                int n = builder.length();
                builder.deleteCharAt(n - 1);
                writer.write(builder.toString());
                writer.write("]\n");
            }
            writer.write("\n");
        }
        if (OUTDEGREE) {
            writer.write("Outdegree centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                centralityAlgorithm = new DegreeCentrality<>(graphs.get(i), false, true);
                StringBuilder builder = new StringBuilder();
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(v + ":" + centralityAlgorithm.getVertexScore(v) + ",");
                }
                int n = builder.length();
                builder.deleteCharAt(n - 1);
                writer.write(builder.toString());
                writer.write("]\n");
            }
            writer.write("\n");
        }
        if (INHARMONIC) {
            writer.write("In-Harmonic centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                centralityAlgorithm = new HarmonicCentrality<>(graphs.get(i), true, true);
                StringBuilder builder = new StringBuilder();
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(v + ":" + centralityAlgorithm.getVertexScore(v) + ",");
                }
                int n = builder.length();
                builder.deleteCharAt(n - 1);
                writer.write(builder.toString());
                writer.write("]\n");
            }
            writer.write("\n");
        }
        if (OUTHARMONIC) {
            writer.write("Out-Harmonic centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                centralityAlgorithm = new HarmonicCentrality<>(graphs.get(i), false, true);
                StringBuilder builder = new StringBuilder();
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(v + ":" + centralityAlgorithm.getVertexScore(v) + ",");
                }
                int n = builder.length();
                builder.deleteCharAt(n - 1);
                writer.write(builder.toString());
                writer.write("]\n");
            }
            writer.write("\n");
        }

        if (BETWEENNESS) {
            writer.write("Betweenness centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                centralityAlgorithm = new BetweennessCentrality<>(graphs.get(i), true);
                StringBuilder builder = new StringBuilder();
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(v + ":" + centralityAlgorithm.getVertexScore(v) + ",");
                }
                int n = builder.length();
                builder.deleteCharAt(n - 1);
                writer.write(builder.toString());
                writer.write("]\n");
            }

        }
        writer.write("\n");
        if (EIGENVECTOR) {
            writer.write("Eigenvector centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                centralityAlgorithm = new EigenvectorCentrality<>(graphs.get(i));
                StringBuilder builder = new StringBuilder();
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(v + ":" + centralityAlgorithm.getVertexScore(v) + ",");
                }
                int n = builder.length();
                builder.deleteCharAt(n - 1);
                writer.write(builder.toString());
                writer.write("]\n");
            }

        }
        writer.write("\n");
        if (KATZ) {
            writer.write("Katz centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                centralityAlgorithm = new KatzCentrality<>(graphs.get(i));
                StringBuilder builder = new StringBuilder();
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(v + ":" + centralityAlgorithm.getVertexScore(v) + ",");
                }
                int n = builder.length();
                builder.deleteCharAt(n - 1);
                writer.write(builder.toString());
                writer.write("]\n");
            }

        }
        writer.write("\n");
        if (PAGERANK) {
            writer.write("PageRank centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                centralityAlgorithm = new PageRank<>(graphs.get(i));
                StringBuilder builder = new StringBuilder();
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(v + ":" + centralityAlgorithm.getVertexScore(v) + ",");
                }
                int n = builder.length();
                builder.deleteCharAt(n - 1);
                writer.write(builder.toString());
                writer.write("]\n");
            }
        }
        writer.write("\n");

        writer.close();

    }

    private static void gmlImporterExample() {
        // got the code through google ai. Added the parameters in the constructor by
        // prompting copilot
        // When I run this code, I get this error message: Error during GML import:
        // The graph contains no vertex supplier
        // copilot wrote that a vertex supplier must be added, and it
        // suggested this change in the code. There are barely any Tutorials on JGraphT
        // on GmlImporter
        // should take a closer look as to why it works
        // ----------------
        Graph<String, DefaultEdge> digraph = new DefaultDirectedGraph<>(SupplierUtil.createStringSupplier(),
                SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GmlImporter<String, DefaultEdge> gmlImporter = new GmlImporter<>();
        try {
            // Import the graph from a GML file
            FileReader reader = new FileReader("data/synthetic/directed/cycle.gml"); // Replace with your GML file path
            gmlImporter.importGraph(digraph, reader);
            reader.close();

            // Now, 'graph' contains the data from the GML file
            System.out.println("Graph imported successfully!");
            System.out.println("Number of vertices: " + digraph.vertexSet().size());
            System.out.println("Number of edges: " + digraph.edgeSet().size());
            System.out.println(digraph);

        } catch (IOException e) {
            System.err.println("Error reading GML file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error during GML import: " + e.getMessage());
        }
        // ------------------
    }

    private static void readAndStoreGmlGraphs(String file, ArrayList<Graph<Integer, DefaultEdge>> graphs,
            ArrayList<String> graphNames,
            boolean isDirected) throws IOException {
    }

    /**
     * Given a list of graphs stored as an edge list.
     * The method reads the graphs and stores the graphs in an array of graph
     * objects.
     * 
     * @param file
     * @param graphs
     * @param graphNames
     * @param isDirected
     * @throws IOException
     */
    private static void readAndStoreInputGraphs(String file, ArrayList<Graph<Integer, DefaultEdge>> graphs,
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
            System.out.println("----- " + graphDirectory.get(i) + " ------");
            System.out.println(graphs.get(i));
        }

    }
}