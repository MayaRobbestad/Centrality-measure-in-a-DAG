package no.uib.mayarobbestad.dagcentrality;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import org.jfree.chart.ChartFactory;
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
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import no.uib.mayarobbestad.dagcentrality.algorithms.DAGPageRankCentrality;
import no.uib.mayarobbestad.dagcentrality.algorithms.DegreeCentrality;
import no.uib.mayarobbestad.dagcentrality.graph.GraphBuilder;

public class Main {

    // builds the graph
    static GraphBuilder builder = new GraphBuilder();

    // The graphs to be run
    static ArrayList<Graph<Integer, DefaultEdge>> graphs = new ArrayList<>();
    static ArrayList<String> graphDirectory = new ArrayList<>();

    // The centrality algorithms to be run
    static final boolean DEGREE = false;
    static final boolean INDEGREE = false;
    static final boolean OUTDEGREE = false;
    static final boolean CLOSENESS = false;
    static final boolean INHARMONIC = false; // variation of closeness
    static final boolean OUTHARMONIC = false;
    static final boolean BETWEENNESS = false;
    static final boolean EIGENVECTOR = false;
    static final boolean KATZ = false;
    static final boolean PAGERANK = false;
    static final boolean DAGCENTRALITY0 = true;

    public static void main(String[] args) throws IOException {
        // readAndStoreInputGraphs("data/dataFiles.txt", graphs, graphDirectory, true);
        readAndStoreGmlGraphs("data/dataFiles.txt", graphs, graphDirectory, true);
        storeCentralityScoresInFile("results/results.txt", graphs);
        storeCentralityScoresInChartCompareGraphs("results/charts/graph comparisons/", graphs);
        storeCentralityScoresInChartCompareMeasures("results/charts/centrality comparisons/", graphs);

    }

    /**
     * 
     * @param folder
     * @param graphs
     * @throws IOException
     */
    private static void storeCentralityScoresInChartCompareMeasures(String folder,
            ArrayList<Graph<Integer, DefaultEdge>> graphs) throws IOException {

        DefaultCategoryDataset dataset;
        for (int i = 0; i < graphs.size(); i++) {
            dataset = new DefaultCategoryDataset();
            VertexScoringAlgorithm<Integer, Double> algorithm;
            if (INDEGREE) {
                algorithm = new DegreeCentrality<>(
                        graphs.get(i), true, true);
                Map<Integer, Double> scores = algorithm.getScores();
                for (Integer v : scores.keySet()) {
                    dataset.addValue(scores.get(v), "in-degree", v);
                }
            }
            if (OUTDEGREE) {
                algorithm = new DegreeCentrality<>(
                        graphs.get(i), false, true);
                Map<Integer, Double> scores = algorithm.getScores();
                for (Integer v : scores.keySet()) {
                    dataset.addValue(scores.get(v), "out-degree", v);
                }
            }
            if (INHARMONIC) {
                algorithm = new HarmonicCentrality<>(graphs.get(i), true, true);
                Map<Integer, Double> scores = algorithm.getScores();
                for (Integer v : scores.keySet()) {
                    dataset.addValue(scores.get(v), "in-harmonic", v);
                }
            }
            if (OUTHARMONIC) {
                algorithm = new HarmonicCentrality<>(graphs.get(i), false, true);
                Map<Integer, Double> scores = algorithm.getScores();
                for (Integer v : scores.keySet()) {
                    dataset.addValue(scores.get(v), "out-harmonic", v);
                }
            }
            if (BETWEENNESS) {
                algorithm = new BetweennessCentrality<>(graphs.get(i), true);
                Map<Integer, Double> scores = algorithm.getScores();
                for (Integer v : scores.keySet()) {
                    dataset.addValue(scores.get(v), "betweeness", v);
                }
            }
            if (EIGENVECTOR) {
                algorithm = new EigenvectorCentrality<>(graphs.get(i));
                Map<Integer, Double> scores = algorithm.getScores();
                for (Integer v : scores.keySet()) {
                    dataset.addValue(scores.get(v), "eigenvector", v);
                }
            }
            if (KATZ) {
                algorithm = new KatzCentrality<>(graphs.get(i));
                Map<Integer, Double> scores = algorithm.getScores();
                for (Integer v : scores.keySet()) {
                    dataset.addValue(scores.get(v), "katz", v);
                }
            }
            if (PAGERANK) {
                algorithm = new PageRank<>(graphs.get(i));
                Map<Integer, Double> scores = algorithm.getScores();
                for (Integer v : scores.keySet()) {
                    dataset.addValue(scores.get(v), "pagerank", v);
                }
            }
            JFreeChart barChart = ChartFactory.createBarChart(graphDirectory.get(i), "Measures", "Centrality score",
                    dataset, PlotOrientation.VERTICAL, true, true, false);

            ChartUtils.saveChartAsPNG(new File(folder + "graph" + i + ".png"), barChart, 650, 400);
        }

    }

    /**
     * Creates and stores charts that compares different graphs based on the same
     * centrality measure
     * 
     * Based on https://coderslegacy.com/java/jfreechart-bar-chart/ and
     * https://www.baeldung.com/jfreechart-visualize-data
     * 
     * @param folder
     * @param graphs
     * @throws IOException
     */
    private static void storeCentralityScoresInChartCompareGraphs(String folder,
            ArrayList<Graph<Integer, DefaultEdge>> graphs)
            throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
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

            ChartUtils.saveChartAsPNG(new File(folder + "indegree.png"), barChart, 650, 400);
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

            ChartUtils.saveChartAsPNG(new File(folder + "outdegree.png"), barChart, 650, 400);
        }
        if (DAGCENTRALITY0) {
            // LIST OF GRAPHS
            // number of double bars
            VertexScoringAlgorithm<Integer, Double> algorithm;
            for (int i = 0; i < graphs.size(); i++) {
                algorithm = new DAGPageRankCentrality<>(graphs.get(i));
                Map<Integer, Double> scores = algorithm.getScores();
                for (Integer v : scores.keySet()) {
                    dataset.addValue(scores.get(v), graphDirectory.get(i), v);
                }
            }

            JFreeChart barChart = ChartFactory.createBarChart("DAG centrality", "Graphs", "Centrality score",
                    dataset, PlotOrientation.VERTICAL, true, true, false);

            ChartUtils.saveChartAsPNG(new File(folder + "DAGPageRank.png"), barChart, 650, 400);
        }

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
            writer.write("\n");
        }
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
            writer.write("\n");
        }
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
            writer.write("\n");
        }
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
            writer.write("\n");
        }

        if (DAGCENTRALITY0) {
            writer.write("Dag centrality version 0\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                // TODO: bad practice fix this
                centralityAlgorithm = new DAGPageRankCentrality<>(graphs.get(i));
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

        writer.close();

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

    /**
     * Given a list of graphs stored in a gml format.
     * The method reads the graphs and stores the graphs in an array of graph
     * objects.
     * 
     * @param file
     * @param graphs
     * @param graphNames
     * @param isDirected
     * @throws IOException
     */
    private static void readAndStoreGmlGraphs(String file, ArrayList<Graph<Integer, DefaultEdge>> graphs,
            ArrayList<String> graphNames,
            boolean isDirected) throws IOException {
        Scanner sc = new Scanner(new FileReader(new File(file)));
        sc.useLocale(Locale.US);
        int n = sc.nextInt();
        sc.nextLine();

        for (int i = 0; i < n; i++) {
            String path = sc.nextLine();
            graphNames.add(i, path);
            graphs.add(i, (Graph<Integer, DefaultEdge>) builder.readGraphFromGmlFile(path, isDirected));
        }
    }

    /**
     * Prints the graph from the array of graphs in the terminal
     * 
     * @param graphs
     */
    public static void printGraphs(List<Graph<Integer, DefaultEdge>> graphs) {
        for (int i = 0; i < graphs.size(); i++) {
            System.out.println("----- " + graphDirectory.get(i) + " ------");
            System.out.println(graphs.get(i));
        }
    }

}