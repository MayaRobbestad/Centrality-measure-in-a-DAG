package no.uib.mayarobbestad.dagcentrality;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

import no.uib.mayarobbestad.dagcentrality.algorithms.DAGBetweennessSourceSinkCentrality;
import no.uib.mayarobbestad.dagcentrality.algorithms.DAGPageRankCentrality;
import no.uib.mayarobbestad.dagcentrality.algorithms.DegreeCentrality;
import no.uib.mayarobbestad.dagcentrality.algorithms.GreedyFAS;
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
    static final boolean KATZ = false;
    static final boolean PAGERANK = false;
    static final boolean DAGPAGERANK = true;
    static final boolean DAGBETWEENNESS = false;

    static ArrayList<Integer> iterationsNeededPerAlgorithm = new ArrayList<>();

    static int numAlgorithms = 0;

    static int iteration = 0;
    static boolean USEITERATIONS = true;

    // algorithm will run MAXITERATIONS - 1 times
    static int MAXITERATIONS = 5; // the number of times the algorithm will run, applicable for PageRank
    // algorithm will run 1 iteration
    static int DEFAULTITERATIONS = 1;

    public static void main(String[] args) throws IOException {
        // readAndStoreInputGraphs("data/dataFiles.txt", graphs, graphDirectory, true);
        readAndStoreGmlGraphs("data/dataFiles.txt", graphs, graphDirectory, true);

        for (Graph<Integer, DefaultEdge> graph : graphs) {
            GreedyFAS.removeCycleFromDirectedGraph(graph);
        }

        storeCentralityScoresInCSV("results/results.csv", graphs);
        readCSVResultsAndStoreScoresInChart("results/results.csv", "results/charts");

        // example of topologica sorting
        /*
         * TopologicalOrderIterator<Integer, DefaultEdge> iterator = new
         * TopologicalOrderIterator<>(graphs.get(0));
         * while (iterator.hasNext()) {
         * System.out.println(iterator.next());
         * }
         */
    }

    private static void readCSVResultsAndStoreScoresInChart(String file, String folder) throws IOException {
        Scanner sc = new Scanner(new FileReader(new File(file)));
        sc.useLocale(Locale.US);
        sc.nextLine(); // skip the first line
        for (int a = 0; a < numAlgorithms; a++) {
            for (int g = 0; g < graphs.size(); g++) {
                for (int i = 0; i < iterationsNeededPerAlgorithm.get(a); i++) {
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                    String currentAlgorithmName = "";
                    String currentGraphName = "";

                    for (Integer v : graphs.get(g).vertexSet()) {
                        String[] column = sc.nextLine().strip().split(",");
                        String algorithmName = column[0];
                        String graphName = column[1];
                        Integer iteration = Integer.parseInt(column[2]);
                        Integer vertex = Integer.parseInt(column[3]);
                        Double score = Double.parseDouble(column[4]);
                        if (currentAlgorithmName.equals("") && currentGraphName.equals("")) {
                            currentAlgorithmName = algorithmName;
                            currentGraphName = graphName;
                        }
                        dataset.addValue(score, graphName + " on iteration " + i, v);
                    }

                    JFreeChart barChart = ChartFactory.createBarChart(
                            currentAlgorithmName + " algorithm on " + currentGraphName + " graph",
                            "Vertices", "Centrality score",
                            dataset, PlotOrientation.VERTICAL, true, true, false);

                    ChartUtils.saveChartAsPNG(
                            new File(
                                    folder + "/" + currentAlgorithmName + "-" + currentGraphName + "-iteration-" + i
                                            + ".png"),
                            barChart,
                            650,
                            400);
                }
            }
        }
    }

    private static void storeCentralityScoresInCSV(String file, ArrayList<Graph<Integer, DefaultEdge>> graphs)
            throws IOException {
        FileWriter writer = new FileWriter(file);
        writer.write("algorithm,graph,iteration,vertex,score\n"); // the columns
        int numGraphs = graphs.size();

        if (DEGREE) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new DegreeCentrality<>(graphs.get(i),
                        false, true);
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(
                            centralityAlgorithm.getClass().getSimpleName() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            numAlgorithms++;
        }
        if (INDEGREE) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new DegreeCentrality<>(graphs.get(i),
                        true, true);
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(
                            centralityAlgorithm.getClass().getSimpleName() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            numAlgorithms++;
        }
        if (OUTDEGREE) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new DegreeCentrality<>(graphs.get(i),
                        false, true);
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(
                            centralityAlgorithm.getClass().getSimpleName() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            numAlgorithms++;
        }
        if (INHARMONIC) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new HarmonicCentrality<>(graphs.get(i),
                        true, true);
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(
                            centralityAlgorithm.getClass().getSimpleName() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            numAlgorithms++;
        }
        if (OUTHARMONIC) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new HarmonicCentrality<>(graphs.get(i),
                        false, true);
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(
                            centralityAlgorithm.getClass().getSimpleName() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            numAlgorithms++;
        }
        if (BETWEENNESS) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new BetweennessCentrality<>(graphs.get(i),
                        true);
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(
                            centralityAlgorithm.getClass().getSimpleName() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            numAlgorithms++;
        }
        if (EIGENVECTOR) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new EigenvectorCentrality<>(
                        graphs.get(i));
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(
                            centralityAlgorithm.getClass().getSimpleName() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            numAlgorithms++;
        }
        if (KATZ) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new KatzCentrality<>(graphs.get(i));
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(
                            centralityAlgorithm.getClass().getSimpleName() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            numAlgorithms++;
        }
        if (PAGERANK) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new PageRank<>(graphs.get(i));
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(
                            centralityAlgorithm.getClass().getSimpleName() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            numAlgorithms++;
        }
        if (DAGPAGERANK && !USEITERATIONS) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new DAGPageRankCentrality<>(
                        graphs.get(i), DEFAULTITERATIONS);
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(
                            centralityAlgorithm.getClass().getSimpleName() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            numAlgorithms++;
        }
        if (DAGPAGERANK && USEITERATIONS) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, MAXITERATIONS);

            for (int i = 0; i < numGraphs; i++) {
                for (int j = 1; j <= MAXITERATIONS; j++) {
                    VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new DAGPageRankCentrality<>(
                            graphs.get(i), j);
                    StringBuilder builder = new StringBuilder();
                    String path = graphDirectory.get(i);
                    String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                    for (Integer v : graphs.get(i).vertexSet()) {
                        builder.append(
                                centralityAlgorithm.getClass().getSimpleName() + ","
                                        + graphName + ","
                                        + j + ","
                                        + v + ","
                                        + centralityAlgorithm.getVertexScore(v) + "\n");
                    }
                    writer.write(builder.toString());
                }
            }
            numAlgorithms++;
        }
        if (DAGBETWEENNESS) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new DAGBetweennessSourceSinkCentrality<>(
                        graphs.get(i));
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(
                            centralityAlgorithm.getClass().getSimpleName() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            numAlgorithms++;
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