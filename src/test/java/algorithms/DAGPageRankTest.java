package algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import no.uib.mayarobbestad.dagcentrality.algorithms.DAGPageRankCentrality;
import no.uib.mayarobbestad.dagcentrality.graph.GraphBuilder;

public class DAGPageRankTest {

    static Graph<Integer, DefaultEdge> graph;
    static GraphBuilder builder = new GraphBuilder();;
    static String randomDag3 = "data/synthetic/directed/format/gml/random-dag-3.gml";

    @BeforeAll
    public static void setup() throws FileNotFoundException {
        graph = builder.readGraphFromGmlFile(randomDag3, true);
    }

    @Test
    public void testScoresNormalizedIteration1() throws FileNotFoundException {
        VertexScoringAlgorithm<Integer, Double> algorithm = new DAGPageRankCentrality<>(graph, 1);
        Map<Integer, Double> scores = algorithm.getScores();
        Map<Integer, Double> expectedScores = new HashMap<>();

        expectedScores.put(0, 0.33333);
        expectedScores.put(1, 1.0);
        expectedScores.put(2, 0.16667);
        expectedScores.put(3, 0.54167);
        expectedScores.put(4, 0.33333);
        expectedScores.put(5, 0.04167);
        expectedScores.put(6, 0.04167);
        expectedScores.put(7, 0.58333);

        for (Integer v : graph.vertexSet()) {
            assertEquals(expectedScores.get(v), scores.get(v), 0.001);
        }
    }

    @Test
    public void testScoresNormalizedIteration2() throws FileNotFoundException {
        VertexScoringAlgorithm<Integer, Double> algorithm = new DAGPageRankCentrality<>(graph, 2);
        Map<Integer, Double> scores = algorithm.getScores();
        Map<Integer, Double> expectedScores = new HashMap<>();

        expectedScores.put(0, 1.33333);
        expectedScores.put(1, 4.0);
        expectedScores.put(2, 0.66667);
        expectedScores.put(3, 2.16667);
        expectedScores.put(4, 1.33333);
        expectedScores.put(5, 0.16667);
        expectedScores.put(6, 0.16667);
        expectedScores.put(7, 2.33333);

        for (Integer v : graph.vertexSet()) {
            assertEquals(expectedScores.get(v), scores.get(v), 0.001);
        }
    }

    @Test
    public void testScoresNormalizedNIterations() throws FileNotFoundException {
        int n = 34;

        VertexScoringAlgorithm<Integer, Double> algorithm = new DAGPageRankCentrality<>(graph, n);
        Map<Integer, Double> scores = algorithm.getScores();
        Map<Integer, Double> expectedScores = new HashMap<>();

        expectedScores.put(0, 1.33333);
        expectedScores.put(1, 4.0);
        expectedScores.put(2, 0.66667);
        expectedScores.put(3, 2.16667);
        expectedScores.put(4, 1.33333);
        expectedScores.put(5, 0.16667);
        expectedScores.put(6, 0.16667);
        expectedScores.put(7, 2.33333);

        for (Integer v : graph.vertexSet()) {
            assertEquals(expectedScores.get(v), scores.get(v), 0.001);
        }
    }

}
