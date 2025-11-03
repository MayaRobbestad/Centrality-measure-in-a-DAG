package no.uib.mayarobbestad.dagcentrality;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.alg.scoring.EigenvectorCentrality;
import org.jgrapht.alg.scoring.HarmonicCentrality;
import org.jgrapht.alg.scoring.KatzCentrality;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultEdge;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import no.uib.mayarobbestad.dagcentrality.algorithms.DegreeCentrality;
import no.uib.mayarobbestad.dagcentrality.algorithms.Distribution;
import no.uib.mayarobbestad.dagcentrality.algorithms.GreedyFASHeuristic;
import no.uib.mayarobbestad.dagcentrality.algorithms.Reach;
import no.uib.mayarobbestad.dagcentrality.algorithms.SAASBetweenness;
import no.uib.mayarobbestad.dagcentrality.datastructures.VertexScore;
import no.uib.mayarobbestad.dagcentrality.graph.GraphBuilder;

public class Main {

    // builds the graph
    static GraphBuilder builder = new GraphBuilder();

    // The graphs to be run
    static ArrayList<Graph<Integer, DefaultEdge>> graphs = new ArrayList<>();
    static ArrayList<String> graphDirectory = new ArrayList<>();
    static List<String> algorithmNames = new ArrayList<>();

    // The centrality algorithms that can be run
    // Centrality algorithms from JGraphT
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
    // Centrality algorithms for DAGs
    static final boolean DISTRIBUTION = true;
    static final boolean REACH = false;
    static final boolean DEPENDENCY = false;
    static final boolean SAAS_BETWEENNESS = true;

    static ArrayList<Integer> iterationsNeededPerAlgorithm = new ArrayList<>();

    static int numAlgorithms = 0;

    static int iteration = 0;
    static boolean USEITERATIONS = false;

    // algorithm will run MAXITERATIONS - 1 times
    static int MAXITERATIONS = 10; // the number of times the algorithm will run, applicable for PageRank

    // algorithm will run 1 iteration
    // Slightly misleading for PageRank, since we will do the maxiterations, but we
    // will only show the score of the final score
    // algorithms that don't need iterating get 0
    // Misleading for DAGPageRank, because eventhough it uses x iterations, the name
    // of the file is
    // 0 iterations
    static int DEFAULTITERATIONS = 0;

    public static void main(String[] args) throws IOException, BadElementException, DocumentException {
        readAndStoreGmlGraphs("data/dataFiles.txt", graphs, graphDirectory, true);
        for (Graph<Integer, DefaultEdge> graph : graphs) {
            GreedyFASHeuristic.removeCycleFromDirectedGraph(graph);

        }
        storeCentralityScoresInCSV("results/results.csv", graphs);
        // storeRuntimeOfAlgorithmsInCSV("results/runtime/timings.csv");
        // readRuntimesFromCSVStoreInChart("results/runtime/timings.csv",
        // "results/runtime/charts/");
        // generatePDFFromImage("results/runtime/charts/chart.png");
        // findXHighestVertices("results/results.csv",
        // "results/highestScores/highest.csv", "Reach", "jdk", 0, 10);

    }

    private static void extracted() throws IOException, BadElementException, MalformedURLException, DocumentException {

        readAndStoreGmlGraphs("data/dataFiles.txt", graphs, graphDirectory, true);

        for (Graph<Integer, DefaultEdge> graph : graphs) {
            GreedyFASHeuristic.removeCycleFromDirectedGraph(graph);

        }

        findSourceAndSinkVertices("data/info/sourceAndSinksPre2.csv");
        for (Graph<Integer, DefaultEdge> graph : graphs) {
            GreedyFASHeuristic.removeCycleFromDirectedGraph(graph);

        }
        findSourceAndSinkVertices("data/info/sourceAndSinksPost2.csv");

        storeCentralityScoresInCSV("results/results.csv", graphs);
        storeRuntimeOfAlgorithmsInCSV("results/runtime/timings.csv");
        // needs to be run prior to runtime charts

        storeGraphAndCentralityInformationForDrawingInGephi("results/results.csv",
                "results/graphVisualization/");

        readCSVResultsAndStoreScoresInChart("results/results.csv",
                "results/charts");
        storeRuntimeOfAlgorithmsInCSV("results/runtime/timings.csv");

        readRuntimesFromCSVStoreInChartDummy("results/runtime/runtime_distribution_and_SAAS-Betweenness.csv",
                "results/runtime/charts/");

        generatePDFFromImage("results/runtime/charts/chart.png");
        readRuntimesFromCSVStoreInChartDummy("results/runtime/runtime_distribution_and_SAAS-Betweenness.csv",
                "results/runtime/charts/");

        readRuntimesFromCSVStoreInChartDummy("results/runtime/timings.csv", "results/runtime/charts/");
        generatePDFFromImage("results/runtime/charts/chart.png");

        // findXHighestVertices("results/results.csv","results/highestScores/highest.csv",
        // "DAGPageRankCentrality", "jdk", 1, 3);

    }

    private static void findSourceAndSinkVertices(String file) throws IOException {
        // graphs, graphDirectory
        FileWriter writer = new FileWriter(file);
        writer.write("graphName,n,m,sources,sources,singles,Network\n"); // the columns

        for (int i = 0; i < graphs.size(); i++) {
            StringBuilder builder = new StringBuilder();
            String path = graphDirectory.get(i);
            String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");

            int sources = 0;
            int sinks = 0;
            int singles = 0;
            Graph<Integer, DefaultEdge> g = graphs.get(i);
            for (Integer v : g.vertexSet()) {
                if (g.inDegreeOf(v) == 0 && g.outDegreeOf(v) > 0) {
                    sources++;
                }
                if (g.outDegreeOf(v) == 0 && g.inDegreeOf(v) > 0) {
                    sinks++;
                }
                if (g.outDegreeOf(v) == 0 && g.inDegreeOf(v) == 0) {
                    singles++;
                }
            }
            builder.append(
                    graphName + "," + g.vertexSet().size() + "," + g.edgeSet().size() + "," + sources + "," + sinks
                            + "," + singles + "," + "Software dependencies" + "\n");
            writer.write(builder.toString());
        }
        writer.close();

    }

    private static void generatePDFFromImage(String input)
            throws BadElementException, MalformedURLException, DocumentException, IOException {

        Image img = Image.getInstance(input);
        float imgWidth = img.getWidth();
        float imgHeight = img.getHeight();
        Document document = new Document(new Rectangle(imgWidth, imgHeight));

        String filePath = input.split("\\.")[0];

        String output = filePath + ".pdf";
        FileOutputStream fos = new FileOutputStream(output);

        PdfWriter writer = PdfWriter.getInstance(document, fos);
        writer.open();
        document.open();
        img.setAbsolutePosition(0, 0);
        document.add(img);
        document.close();
        writer.close();
    }

    private static void readRuntimesFromCSVStoreInChartDummy(String file, String folder) throws IOException {
        Scanner sc = new Scanner(new FileReader(new File(file)));
        sc.useLocale(Locale.US);
        sc.nextLine(); // skip the first line
        // DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<String> names = new ArrayList<>();
        // names.add("Distribution");
        names.add("Reach");
        names.add("Dependency");
        // names.add("SAAS-Betweenness");
        XYSeriesCollection series = new XYSeriesCollection();
        for (int a = 0; a < 2; a++) {
            XYSeries dataset = new XYSeries(names.get(a), true);
            for (int g = 0; g < 17; g++) {

                String currentAlgorithmName = "";
                String currentGraphName = "";

                String[] column = sc.nextLine().strip().split(",");
                String algorithmName = column[0];
                String graphName = column[1];
                Integer iteration = Integer.parseInt(column[2]);
                Integer n = Integer.parseInt(column[3]);
                Integer m = Integer.parseInt(column[4]);
                Double timeInSeconds = Double.parseDouble(column[5]);
                // Long timeInMicro = Long.parseLong(column[4]);

                if (currentAlgorithmName.equals("") && currentGraphName.equals("")) {
                    currentAlgorithmName = algorithmName;
                    currentGraphName = graphName;
                }
                // change to m
                dataset.add(m, timeInSeconds);

            }
            series.addSeries(dataset);
            // sc.close();
        }

        JFreeChart xyPlot = ChartFactory.createXYLineChart("", "n", "time (in seconds)", series);
        XYPlot plot = xyPlot.getXYPlot();
        plot.setDomainAxis(new NumberAxis("m"));
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setDefaultLinesVisible(false);
        renderer.setDefaultShapesVisible(true);
        plot.setRenderer(renderer);

        int width = 640; /* Width of the image */
        int height = 480; /* Height of the image */
        File runtimeChart = new File(folder + "chart.png");
        ChartUtils.saveChartAsPNG(runtimeChart, xyPlot, width, height);

    }

    private static void readRuntimesFromCSVStoreInChart(String file, String folder) throws IOException {
        Scanner sc = new Scanner(new FileReader(new File(file)));
        sc.useLocale(Locale.US);
        sc.nextLine(); // skip the first line
        // DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        XYSeriesCollection series = new XYSeriesCollection();
        for (int a = 0; a < numAlgorithms; a++) {
            XYSeries dataset = new XYSeries(algorithmNames.get(a), true);
            for (int g = 0; g < graphs.size(); g++) {
                for (int i = 0; i <= iterationsNeededPerAlgorithm.get(a); i++) {

                    String currentAlgorithmName = "";
                    String currentGraphName = "";

                    String[] column = sc.nextLine().strip().split(",");
                    String algorithmName = column[0];
                    String graphName = column[1];
                    Integer iteration = Integer.parseInt(column[2]);
                    Integer n = Integer.parseInt(column[3]);
                    Integer m = Integer.parseInt(column[4]);
                    Double timeInSeconds = Double.parseDouble(column[5]);
                    if (timeInSeconds == -1) {
                        continue;
                    }
                    // Long timeInMicro = Long.parseLong(column[4]);

                    if (currentAlgorithmName.equals("") && currentGraphName.equals("")) {
                        currentAlgorithmName = algorithmName;
                        currentGraphName = graphName;
                    }
                    // change to m
                    dataset.add(m, timeInSeconds);
                }
            }
            series.addSeries(dataset);
        }

        JFreeChart xyPlot = ChartFactory.createXYLineChart("", "n", "time (in seconds)", series);
        XYPlot plot = xyPlot.getXYPlot();
        plot.setDomainAxis(new NumberAxis("m"));
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setDefaultLinesVisible(false);
        renderer.setDefaultShapesVisible(true);
        plot.setRenderer(renderer);

        int width = 640; /* Width of the image */
        int height = 480; /* Height of the image */
        File runtimeChart = new File(folder + "chart.png");
        ChartUtils.saveChartAsPNG(runtimeChart, xyPlot, width, height);

    }

    private static void storeGraphAndCentralityInformationForDrawingInGephi(String resultsFile,
            String visualizationFolder) throws IOException {

        Scanner sc = new Scanner(new FileReader(new File(resultsFile)));
        sc.useLocale(Locale.US);
        sc.nextLine(); // skip the first line

        for (int a = 0; a < numAlgorithms; a++) {
            for (int g = 0; g < graphs.size(); g++) {

                for (int i = 0; i <= iterationsNeededPerAlgorithm.get(a); i++) {

                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                    String currentAlgorithmName = "";
                    String currentGraphName = "";
                    List<String> entries = new ArrayList<>();

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
                        String entry = vertex + "," + vertex + "," + score;
                        entries.add(entry);

                    }
                    // creting a file for each graph only needs to be doe once
                    // not necessary

                    if (a == 0) {
                        createGraphEdgesFile(currentGraphName, g, visualizationFolder);
                    }

                    createGraphNodesWithScores(currentGraphName, currentAlgorithmName, entries, visualizationFolder);

                }
            }
        }

    }

    private static void createGraphNodesWithScores(String currentGraphName, String currentAlgorithmName,
            List<String> entries, String visualizationFolder) throws IOException {
        File file = new File(visualizationFolder + currentAlgorithmName + "_" + currentGraphName + "_nodes.csv");
        FileWriter writer = new FileWriter(file);

        writer.write("Id,Label,Score\n"); // the columns
        for (String entry : entries) {
            writer.write(entry + "\n");

        }
        writer.close();

    }

    private static void createGraphEdgesFile(String currentGraphName, Integer graphId, String visualizationFolder)
            throws IOException {
        File file = new File(visualizationFolder + currentGraphName + "_edges.csv");
        FileWriter writer = new FileWriter(file);
        writer.write("Source,Target,Type\n"); // the columns
        Graph<Integer, DefaultEdge> currentGraph = graphs.get(graphId);
        for (DefaultEdge e : currentGraph.edgeSet()) {
            Integer source = currentGraph.getEdgeSource(e);
            Integer target = currentGraph.getEdgeTarget(e);
            String type = "Directed";
            writer.write(source + "," + target + "," + type + "\n");
            // System.out.print(source + "," + target + "," + type + "\n");
        }
        writer.close();

    }

    public static long timeAlgorithmWithTimeLimit(VertexScoringAlgorithm<Integer, BigDecimal> algorithm) {
        long startTime = System.nanoTime();
        // this method calls on compute, might be some
        // extra time with checking wether or not the scores are null
        Runnable runAlgorithm = new Thread() {
            @Override
            public void run() {
                // the algorithm is run by calling this method
                algorithm.getScores();
            }
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(runAlgorithm);
        executor.shutdown();
        long timeElapsed;
        try {
            future.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            /* Handle the interruption. Or ignore it. */
        } catch (ExecutionException ee) {
            /* Handle the error. Or ignore it. */
        } catch (TimeoutException te) {
            /* Handle the timeout. Or ignore it. */
        }
        if (!executor.isTerminated()) {
            executor.shutdownNow(); // If you want to stop the code that hasn't finished.
            timeElapsed = -1;
        } else {
            long endTime = System.nanoTime();
            timeElapsed = (endTime - startTime);
        }

        return timeElapsed;
    }

    private static void storeRuntimeOfAlgorithmsInCSV(String file) throws IOException {
        FileWriter writer = new FileWriter(file);
        writer.write("algorithm,graph,iteration,vertices,edges,time\n"); // the columns
        int numGraphs = graphs.size();
        if (DISTRIBUTION) {
            // writer.write("algorithm,graph,iteration,n,time\n"); // the columns
            for (int i = 0; i < numGraphs; i++) {
                System.out.println("DISTRIBUTION");
                System.out.println("graph" + i);
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                VertexScoringAlgorithm centralityAlgorithm = new Distribution<>(graphs.get(i), MAXITERATIONS);
                int n = graphs.get(i).vertexSet().size();
                int m = graphs.get(i).edgeSet().size();
                // this is where the algorithm is run with .getScores
                long timeElapsedMicro = timeAlgorithmWithTimeLimit(centralityAlgorithm) / 1000;
                double timeElapsedSeconds;
                if (timeElapsedMicro < 0) {
                    timeElapsedSeconds = -1;
                } else {
                    timeElapsedSeconds = (timeElapsedMicro / 1000000.0);
                }

                StringBuilder builder = new StringBuilder();
                builder.append(
                        centralityAlgorithm.getClass().getSimpleName() + ","
                                + graphName + ","
                                + DEFAULTITERATIONS + ","
                                + n + ","
                                + m + ","
                                + timeElapsedSeconds + "\n");
                writer.write(builder.toString());
            }
        }
        if (REACH) {
            for (int i = 0; i < numGraphs; i++) {
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                VertexScoringAlgorithm centralityAlgorithm = new Reach<>(graphs.get(i), false, true);
                int n = graphs.get(i).vertexSet().size();
                int m = graphs.get(i).edgeSet().size();
                // this is where the algorithm is run with .getScores
                long timeElapsedMicro = timeAlgorithmWithTimeLimit(centralityAlgorithm) / 1000;
                double timeElapsedSeconds = (timeElapsedMicro / 1000000.0);
                StringBuilder builder = new StringBuilder();
                builder.append(
                        centralityAlgorithm.getClass().getSimpleName() + ","
                                + graphName + ","
                                + DEFAULTITERATIONS + ","
                                + n + ","
                                + m + ","
                                + timeElapsedSeconds + "\n");
                writer.write(builder.toString());
            }
        }

        if (DEPENDENCY) {
            for (int i = 0; i < numGraphs; i++) {
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                VertexScoringAlgorithm centralityAlgorithm = new Reach<>(graphs.get(i), false, true);
                int n = graphs.get(i).vertexSet().size();
                int m = graphs.get(i).edgeSet().size();
                // this is where the algorithm is run with .getScores
                long timeElapsedMicro = timeAlgorithmWithTimeLimit(centralityAlgorithm) / 1000;
                double timeElapsedSeconds = (timeElapsedMicro / 1000000.0);
                StringBuilder builder = new StringBuilder();
                builder.append(
                        centralityAlgorithm.getClass().getSimpleName() + ","
                                + graphName + ","
                                + DEFAULTITERATIONS + ","
                                + n + ","
                                + m + ","
                                + timeElapsedSeconds + "\n");
                writer.write(builder.toString());
            }
        }

        if (SAAS_BETWEENNESS) {
            for (int i = 0; i < numGraphs; i++) {
                System.out.println("SAAS_BETWEENNESS");
                System.out.println("graph" + i);
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                VertexScoringAlgorithm centralityAlgorithm = new SAASBetweenness<>(graphs.get(i));
                int n = graphs.get(i).vertexSet().size();
                int m = graphs.get(i).edgeSet().size();
                // this is where the algorithm is run with .getScores
                long timeElapsedMicro = timeAlgorithmWithTimeLimit(centralityAlgorithm) / 1000;
                double timeElapsedSeconds = (timeElapsedMicro / 1000000.0);
                StringBuilder builder = new StringBuilder();
                builder.append(
                        centralityAlgorithm.getClass().getSimpleName() + ","
                                + graphName + ","
                                + DEFAULTITERATIONS + ","
                                + n + ","
                                + m + ","
                                + timeElapsedSeconds + "\n");
                writer.write(builder.toString());
            }
        }
        writer.close();
        // TODO: implement for other centrality measures

    }

    private static void findXHighestVertices(String file, String file2, String algorithm, String graph,
            Integer iteration, int x)
            throws IOException {
        Scanner sc = new Scanner(new FileReader(new File(file)));
        sc.useLocale(Locale.US);
        String header = sc.nextLine(); // skip the first line

        List<VertexScore> filteredResults = new ArrayList<>();
        // we don't want to continue looking through the output if we have already found
        // our entries
        int check = -1;
        while (sc.hasNext()) {
            String[] column = sc.nextLine().strip().split(",");
            // only add the result o the list if they
            if (column[0].equals(algorithm) && column[1].equals(graph) && Integer.parseInt(column[2]) == iteration) {
                check = 0;
                filteredResults.add(new VertexScore(column[0], column[1], Integer.parseInt(column[2]),
                        Integer.parseInt(column[3]), Double.parseDouble(column[4])));
            }
            // we have seen all the entries that fulfill our criteria's
            else if (check == 0) {
                check = 1;
                break;
            }
        }
        FileWriter writer = new FileWriter(file2);
        writer.write(header);

        // smallest scores first
        Collections.sort(filteredResults);

        List<VertexScore> xHighest = filteredResults.subList(filteredResults.size() - x, filteredResults.size());

        for (VertexScore vertexScore : xHighest) {
            String toWrite = vertexScore.getAlgorithm() + ","
                    + vertexScore.getGraph() + ","
                    + vertexScore.getIteration() + ","
                    + vertexScore.getVertex() + ","
                    + vertexScore.getScore() + "\n";
            System.out.println(toWrite);
            writer.write(toWrite);
        }
        writer.close();
    }

    private static void readCSVResultsAndStoreScoresInChart(String file, String folder) throws IOException {
        Scanner sc = new Scanner(new FileReader(new File(file)));
        sc.useLocale(Locale.US);
        sc.nextLine(); // skip the first line
        for (int a = 0; a < numAlgorithms; a++) {
            for (int g = 0; g < graphs.size(); g++) {
                for (int i = 0; i <= iterationsNeededPerAlgorithm.get(a); i++) {
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                    String currentAlgorithmName = "";
                    String currentGraphName = "";

                    // TODO: remove later
                    // String[] alphabet = { "A", "B", "C", "D", "E", "F", "G", "H" };

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
                        dataset.addValue(score, graphName + " in iteration " + i, v);
                        // dataset.addValue(score, graphName + " in iteration " + i, alphabet[v]);
                    }
                    // title =currentAlgorithmName + " algorithm on " + currentGraphName + " graph"
                    JFreeChart barChart = ChartFactory.createBarChart(
                            "currentAlgorithmName + \" algorithm on \" + currentGraphName + \" graph\"v",
                            "Vertices", "Centrality Score",
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

    /**
     * The centrality measures that are set to true will run on the list of graphs
     * given.
     * The results of the algorithms on a graph are stored in a given file.
     */
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
                            centralityAlgorithm.toString() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            algorithmNames.add("Degree");
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
                            centralityAlgorithm.toString() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            algorithmNames.add("In-Degree");
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
                            centralityAlgorithm.toString() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v) + "\n");
                }
                writer.write(builder.toString());
            }
            algorithmNames.add("Out-Degree");
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
            algorithmNames.add("In-Harmonic");
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
            algorithmNames.add("Out-Harmonic");
            numAlgorithms++;
        }
        if (BETWEENNESS) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, Double> centralityAlgorithm = new BetweennessCentrality<>(graphs.get(i),
                        false);
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
            algorithmNames.add("Betweenness");
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
            algorithmNames.add("Eigenvector");
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
            algorithmNames.add("Katz");
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
                                    + centralityAlgorithm.getVertexScore(v).toString() + "\n");
                }
                writer.write(builder.toString());
            }
            algorithmNames.add("PageRank");
            numAlgorithms++;
        }
        if (DISTRIBUTION && !USEITERATIONS) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                System.out.println("distribution");
                System.out.println("graph num" + i);
                VertexScoringAlgorithm<Integer, BigDecimal> centralityAlgorithm = new Distribution<>(
                        graphs.get(i), MAXITERATIONS);
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                getResultsWithinTimeLimit(graphs, i, centralityAlgorithm, builder,
                        graphName);
                // centralityAlgorithm.getScores();
                /*
                 * for (Integer v : graphs.get(i).vertexSet()) {
                 * builder.append(
                 * centralityAlgorithm.getClass().getSimpleName() + ","
                 * + graphName + ","
                 * + DEFAULTITERATIONS + ","
                 * + v + ","
                 * + centralityAlgorithm.getVertexScore(v) + "\n");
                 * }
                 */
                writer.write(builder.toString());
            }
            algorithmNames.add("Distribution");
            numAlgorithms++;
        }
        if (DISTRIBUTION && USEITERATIONS) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, MAXITERATIONS);

            for (int i = 0; i < numGraphs; i++) {
                for (int j = 0; j <= MAXITERATIONS; j++) {
                    VertexScoringAlgorithm<Integer, BigDecimal> centralityAlgorithm = new Distribution<>(
                            graphs.get(i), j);
                    StringBuilder builder = new StringBuilder();
                    String path = graphDirectory.get(i);
                    String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                    getResultsWithinTimeLimit(graphs, i, centralityAlgorithm, builder,
                            graphName);
                    /*
                     * centralityAlgorithm.getScores();
                     * for (Integer v : graphs.get(i).vertexSet()) {
                     * builder.append(
                     * centralityAlgorithm.getClass().getSimpleName() + ","
                     * + graphName + ","
                     * + DEFAULTITERATIONS + ","
                     * + v + ","
                     * + centralityAlgorithm.getVertexScore(v) + "\n");
                     * }
                     */
                    writer.write(builder.toString());
                }
            }
            algorithmNames.add("Distribution");
            numAlgorithms++;
        }

        if (REACH) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, BigDecimal> centralityAlgorithm = new Reach<>(
                        graphs.get(i), true, true);
                centralityAlgorithm.getScores(); // run the algorithm
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                // getResultsWithinTimeLimit(graphs, i, centralityAlgorithm, builder,graphName

                for (Integer v : graphs.get(i).vertexSet()) {
                    builder.append(
                            centralityAlgorithm.getClass().getSimpleName() + ","
                                    + graphName + ","
                                    + DEFAULTITERATIONS + ","
                                    + v + ","
                                    + centralityAlgorithm.getVertexScore(v).toString() + "\n");
                }

                writer.write(builder.toString());
            }
            algorithmNames.add("Reach");
            numAlgorithms++;
        }

        if (DEPENDENCY) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                VertexScoringAlgorithm<Integer, BigDecimal> centralityAlgorithm = new Reach<>(
                        graphs.get(i), true, false);
                centralityAlgorithm.getScores(); // run the algorithm
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");
                getResultsWithinTimeLimit(graphs, i, centralityAlgorithm, builder,
                        graphName);
                /*
                 * for (Integer v : graphs.get(i).vertexSet()) {
                 * builder.append(
                 * centralityAlgorithm.getClass().getSimpleName() + ","
                 * + graphName + ","
                 * + DEFAULTITERATIONS + ","
                 * + v + ","
                 * + centralityAlgorithm.getVertexScore(v).toString() + "\n");
                 * }
                 */
                writer.write(builder.toString());
            }
            algorithmNames.add("Dependency");
            numAlgorithms++;
        }

        if (SAAS_BETWEENNESS) {
            iterationsNeededPerAlgorithm.add(numAlgorithms, DEFAULTITERATIONS);
            for (int i = 0; i < numGraphs; i++) {
                System.out.println("SAAS_BETWEENNESS");
                System.out.println("graph num" + i);
                VertexScoringAlgorithm<Integer, BigDecimal> centralityAlgorithm = new SAASBetweenness<>(
                        graphs.get(i));
                StringBuilder builder = new StringBuilder();
                String path = graphDirectory.get(i);
                String graphName = path.substring(path.lastIndexOf("/") + 1).replace(".gml", "");

                getResultsWithinTimeLimit(graphs, i, centralityAlgorithm, builder,
                        graphName);
                /*
                 * centralityAlgorithm.getScores();
                 * for (Integer v : graphs.get(i).vertexSet()) {
                 * BigDecimal score = centralityAlgorithm.getVertexScore(v);
                 * if (score == null) {
                 * score = new BigDecimal("0");
                 * }
                 * builder.append(
                 * centralityAlgorithm.getClass().getSimpleName() + ","
                 * + graphName + ","
                 * + DEFAULTITERATIONS + ","
                 * + v + ","
                 * + score.toString() + "\n");
                 * }
                 */
                writer.write(builder.toString());
            }
            algorithmNames.add("SAAS-Betweenness");
            numAlgorithms++;
        }
        writer.close();
    }

    /**
     * The soltioin is found in stackoverflow
     * https://stackoverflow.com/questions/5715235/java-set-timeout-on-a-certain-block-of-code
     */
    private static void getResultsWithinTimeLimit(ArrayList<Graph<Integer, DefaultEdge>> graphs, int i,
            VertexScoringAlgorithm<Integer, BigDecimal> centralityAlgorithm, StringBuilder builder, String graphName) {
        Runnable runAlgorithm = new Thread() {
            @Override
            public void run() {
                // the algorithm is run by calling this method
                centralityAlgorithm.getScores();
            }
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(runAlgorithm);
        executor.shutdown();

        try {
            future.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            /* Handle the interruption. Or ignore it. */
        } catch (ExecutionException ee) {
            /* Handle the error. Or ignore it. */
        } catch (TimeoutException te) {
            /* Handle the timeout. Or ignore it. */
        }
        if (!executor.isTerminated()) {
            executor.shutdownNow(); // If you want to stop the code that hasn't finished.
            System.out.println("Stopped running");
            for (Integer v : graphs.get(i).vertexSet()) {
                builder.append(
                        centralityAlgorithm.getClass().getSimpleName() + ","
                                + graphName + ","
                                + DEFAULTITERATIONS + ","
                                + v + ","
                                + 0 + "\n");
            }
        } else {
            for (Integer v : graphs.get(i).vertexSet()) {
                builder.append(
                        centralityAlgorithm.getClass().getSimpleName() + ","
                                + graphName + ","
                                + DEFAULTITERATIONS + ","
                                + v + ","
                                + centralityAlgorithm.getVertexScore(v) + "\n");
            }
        }
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