package no.uib.mayarobbestad.dagcentrality;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

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
    static final boolean INDEGREE = false;
    static final boolean OUTDEGREE = false;
    static final boolean CLOSENESS = false;
    static final boolean INHARMONIC = false; // variation of closeness
    static final boolean OUTHARMONIC = false;
    static final boolean BETWEENNESS = false;
    static final boolean EIGENVECTOR = false;
    static final boolean KATZ = false;
    static final boolean PAGERANK = false;
    static final boolean DAGPAGERANK = true;
    static final boolean DAGBETWEENNESS = false;

    static ArrayList<Integer> iterationsNeededPerAlgorithm = new ArrayList<>();

    static int numAlgorithms = 0;

    static int iteration = 0;
    static boolean USEITERATIONS = true;

    static int MAXITERATIONS = 5; // the number of times the algorithm will run, applicable for PageRank
    static int DEFAULTITERATIONS = 1;

    public static void main(String[] args) throws IOException {
        // readAndStoreInputGraphs("data/dataFiles.txt", graphs, graphDirectory, true);
        readAndStoreGmlGraphs("data/dataFiles.txt", graphs, graphDirectory, true);

        for (Graph<Integer, DefaultEdge> graph : graphs) {
            Set<DefaultEdge> removed = GreedyFAS.removeCycleFromDirectedGraph(graph);
            System.out.println("removed:" + removed);
        }

        // storeCentralityScoresInFile("results/results.txt", graphs);

        storeCentralityScoresInCSV("results/results.csv", graphs);

        readCSVResultsAndStoreScoresInChart("results/results.csv", "results/charts");

        // readResultsAndStoreScoresInChart("results/results.txt", "results/charts");

        // only works for PageRank, when we want to visualize the iterations

        /*
         * for (int i = 1; i < MAXITERATIONS; i++) {
         * iteration = i;
         * storeCentralityScoresInFile("results/results.txt", graphs);
         * // storeCentralityScoresInCSV("results/results.csv", graphs);
         * readResultsAndStoreScoresInChart("results/results.txt", "results/charts");
         * numAlgorithms = 0; // reset number of algorithms
         * }
         */

        // findNMostCentralVertices("results/results.txt", 5);

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
                    String[] column = sc.nextLine().strip().split(",");

                    String currentAlgorithmName = column[0];
                    String currentGraphName = column[1];

                    Set<Integer> iterable = graphs.get(g).vertexSet();
                    int count = 0;
                    for (Integer v : iterable) {
                        String algorithmName = column[0];
                        String graphName = column[1];
                        Integer iteration = Integer.parseInt(column[2]);
                        Integer vertex = Integer.parseInt(column[3]);
                        Double score = Double.parseDouble(column[4]);
                        dataset.addValue(score, graphName, v);
                        // TODO
                        count++;
                        if (count < iterable.size() - 1) {
                            column = sc.nextLine().strip().split(",");
                        }
                    }

                    JFreeChart barChart = ChartFactory.createBarChart(currentGraphName,
                            "Vertices", "Centrality score",
                            dataset, PlotOrientation.VERTICAL, true, true, false);

                    ChartUtils.saveChartAsPNG(
                            new File(
                                    folder + "/all/" + currentAlgorithmName + "-" + currentGraphName + "-iteration-" + i
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
        System.out.println("here");
        if (DEGREE) {
            iterationsNeededPerAlgorithm.add(DEFAULTITERATIONS);

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

        if (DAGPAGERANK && !USEITERATIONS) {
            iterationsNeededPerAlgorithm.add(DEFAULTITERATIONS);

            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new DAGPageRankCentrality<>(
                        graphs.get(i), MAXITERATIONS);
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(
                            centralityAlgorithm.getClass().getSimpleName() + ","
                                    + graphName + ","
                                    + MAXITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            numAlgorithms++;
        }

        if (DAGPAGERANK && USEITERATIONS) {
            iterationsNeededPerAlgorithm.add(MAXITERATIONS);

            for (int i = 0; i < numGraphs; i++) {
                for (int j = 1; j < MAXITERATIONS; j++) {
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

        writer.close();
    }

    /**
     * Reads the results in the results file, and makes a chart based on the
     * centrality scores.
     * 
     * @param string
     * @throws IOException
     */
    private static void readResultsAndStoreScoresInChart(String file, String folder) throws IOException {
        Scanner sc = new Scanner(new FileReader(new File(file)));
        sc.useLocale(Locale.US);
        for (int a = 0; a < numAlgorithms; a++) {
            String algorithmName = sc.nextLine();
            for (int g = 0; g < graphs.size(); g++) {
                String graphResult = sc.nextLine();
                makeChart(algorithmName, graphResult, folder);
            }
            sc.nextLine(); // empty line
        }
        sc.close();
    }

    private static void makeChart(String algorithmName, String graphResult, String folder) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String[] parts = graphResult.strip().split("="); // divide into path and results
        String path = parts[0].trim(); // the path of the graph
        String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");

        String resultList = parts[1].trim();
        String results = resultList.substring(1, resultList.length() - 1); // remove [ and ]

        for (String pair : results.split(",")) {
            String[] entry = pair.split(":");
            Integer vertex = Integer.parseInt(entry[0]);
            Double score = Double.parseDouble(entry[1]);
            dataset.addValue(score, graphName, vertex);
        }

        JFreeChart barChart = ChartFactory.createBarChart(algorithmName, "Vertices", "Centrality score",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        // TODO: change this to safe the results into seperate folders, instead of
        // everything in one folder
        if (USEITERATIONS) {
            ChartUtils.saveChartAsPNG(
                    new File(folder + "/compareIterations/" + graphName + algorithmName + "iteration" + iteration
                            + ".png"),
                    barChart,
                    650,
                    400);
        } else {
            ChartUtils.saveChartAsPNG(new File(folder + "/finalMeasure/" + graphName + algorithmName + ".png"),
                    barChart,
                    650,
                    400);
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

        if (DEGREE) {
            /*
             * Also possible, instead of writing the name directly
             * centralityAlgorithm = new DegreeCentrality<>(graphs.get(0), true);
             * writer.write(centralityAlgorithm.getClass().getSimpleName() + "\n");
             */
            writer.write("Degree centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                // makes sure that all vertices are written in the file, eventhopugh we might
                // have deleted some vertieces previously
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new DegreeCentrality<>(graphs.get(i),
                        false, true);

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
            numAlgorithms++;
        }
        if (INDEGREE) {
            writer.write("Indegree centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new DegreeCentrality<>(graphs.get(i),
                        true, true);
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
            numAlgorithms++;
        }
        if (OUTDEGREE) {
            writer.write("Outdegree centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new DegreeCentrality<>(graphs.get(i),
                        false, true);
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
            numAlgorithms++;
        }
        if (INHARMONIC) {
            writer.write("In-Harmonic centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new HarmonicCentrality<>(graphs.get(i),
                        true, true);
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
            numAlgorithms++;
        }
        if (OUTHARMONIC) {
            writer.write("Out-Harmonic centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new HarmonicCentrality<>(graphs.get(i),
                        false, true);
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
            numAlgorithms++;
        }

        if (BETWEENNESS) {
            writer.write("Betweenness centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new BetweennessCentrality<>(graphs.get(i),
                        true);
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
            numAlgorithms++;
        }
        if (EIGENVECTOR) {
            writer.write("Eigenvector centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new EigenvectorCentrality<>(
                        graphs.get(i));
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
            numAlgorithms++;
        }
        if (KATZ) {
            writer.write("Katz centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new KatzCentrality<>(graphs.get(i));
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
            numAlgorithms++;
        }
        if (PAGERANK) {
            writer.write("PageRank centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new PageRank<>(graphs.get(i));
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
            numAlgorithms++;
        }

        if (DAGPAGERANK) {

            writer.write("Dag PageRank Centrality\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                // TODO: bad practice fix this
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new DAGPageRankCentrality<>(graphs.get(i),
                        iteration);
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
            numAlgorithms++;
        }

        if (DAGBETWEENNESS) {
            writer.write("Dag centrality version 1\n");
            for (int i = 0; i < numGraphs; i++) {
                writer.write(graphDirectory.get(i) + " = [");
                // TODO: bad practice fix this
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new DAGBetweennessSourceSinkCentrality<>(
                        graphs.get(i));
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