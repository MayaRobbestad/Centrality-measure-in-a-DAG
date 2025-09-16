package algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    static Graph<Integer, DefaultEdge> graph0;
    static Graph<Integer, DefaultEdge> graph3;
    static GraphBuilder builder = new GraphBuilder();;
    static String randomDag3 = "data/synthetic/directed/format/gml/random-dag-3.gml";
    static String randomDag0 = "data/synthetic/directed/format/gml/random-dag-0.gml";

    @BeforeAll
    public static void setup() throws FileNotFoundException {
        graph0 = builder.readGraphFromGmlFile(randomDag0, true);
        graph3 = builder.readGraphFromGmlFile(randomDag3, true);

    }

    @Test
    public void testScoresAfterIteration0onRandomGraph0() throws FileNotFoundException {
        VertexScoringAlgorithm<Integer, BigDecimal> algorithm = new DAGPageRankCentrality<>(graph0, 0);
        Map<Integer, BigDecimal> expectedScores = new HashMap<>();

        expectedScores.put(0, new BigDecimal("1"));
        expectedScores.put(1, new BigDecimal("1"));
        expectedScores.put(2, new BigDecimal("0"));
        expectedScores.put(3, new BigDecimal("0"));
        expectedScores.put(4, new BigDecimal("0"));
        expectedScores.put(5, new BigDecimal("0"));
        expectedScores.put(6, new BigDecimal("0"));
        expectedScores.put(7, new BigDecimal("0"));

        for (Integer v : graph0.vertexSet()) {
            BigDecimal expected = expectedScores.get(v).setScale(3, RoundingMode.HALF_UP);
            BigDecimal actual = algorithm.getVertexScore(v).setScale(3, RoundingMode.HALF_UP);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testScoresAfterIteration1onRandomGraph0() throws FileNotFoundException {
        VertexScoringAlgorithm<Integer, BigDecimal> algorithm = new DAGPageRankCentrality<>(graph0, 1);
        Map<Integer, BigDecimal> expectedScores = new HashMap<>();

        expectedScores.put(0, new BigDecimal("1.000"));
        expectedScores.put(1, new BigDecimal("1.000"));
        expectedScores.put(2, new BigDecimal("0.500"));
        expectedScores.put(3, new BigDecimal("1.000"));
        expectedScores.put(4, new BigDecimal("0.500"));
        expectedScores.put(5, new BigDecimal("0.250"));
        expectedScores.put(6, new BigDecimal("0.250"));
        expectedScores.put(7, new BigDecimal("1.500"));

        for (Integer v : graph0.vertexSet()) {
            BigDecimal expected = expectedScores.get(v).setScale(3, RoundingMode.HALF_UP);
            BigDecimal actual = algorithm.getVertexScore(v).setScale(3, RoundingMode.HALF_UP);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testScoresAfterIteration2onRandomGraph0() throws FileNotFoundException {
        VertexScoringAlgorithm<Integer, BigDecimal> algorithm = new DAGPageRankCentrality<>(graph0, 2);

        Map<Integer, BigDecimal> expectedScores = new HashMap<>();

        expectedScores.put(0, new BigDecimal(2.3333333)); // 7/3
        expectedScores.put(1, new BigDecimal(0.6666666)); // 2/3
        expectedScores.put(2, new BigDecimal(1.1666666)); // 7/6
        expectedScores.put(3, new BigDecimal(1.5)); // 7/6 + 1/3
        expectedScores.put(4, new BigDecimal(0.3333333)); // 1/3
        expectedScores.put(5, new BigDecimal(0.5833333)); // 7/12
        expectedScores.put(6, new BigDecimal(0.5833333)); // 7/12
        expectedScores.put(7, new BigDecimal(1.8333333)); // 7/6 + 2/3

        for (Integer v : graph0.vertexSet()) {
            BigDecimal expected = expectedScores.get(v).setScale(5, RoundingMode.HALF_UP);
            BigDecimal actual = algorithm.getVertexScore(v).setScale(5, RoundingMode.HALF_UP);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testScoresAfterIteration3onRandomGraph0() throws FileNotFoundException {
        VertexScoringAlgorithm<Integer, BigDecimal> algorithm = new DAGPageRankCentrality<>(graph0, 3);

        Map<Integer, BigDecimal> expectedScores = new HashMap<>();

        expectedScores.put(0, new BigDecimal(2.636363636363636363636363636363636363636363636)); // 29/11
        expectedScores.put(1, new BigDecimal(0.363636363636363636363636363636363636363636363)); // 4/11
        expectedScores.put(2, new BigDecimal(1.318181818181818181818181818181818181818181818)); // 29/22
        expectedScores.put(3, new BigDecimal(1.5));// 29/22 + 2/11
        expectedScores.put(4, new BigDecimal(0.181818181818181818181818181818181818181818181)); // 2/11
        expectedScores.put(5, new BigDecimal(0.659090909090909090909090909090909090909090909)); // 29/44
        expectedScores.put(6, new BigDecimal(0.659090909090909090909090909090909090909090909)); // 29/44
        expectedScores.put(7, new BigDecimal(1.681818181818181818181818181818181818181818181)); // 29/22 + 4/11

        for (Integer v : graph0.vertexSet()) {
            BigDecimal expected = expectedScores.get(v).setScale(15, RoundingMode.HALF_UP);
            BigDecimal actual = algorithm.getVertexScore(v).setScale(15, RoundingMode.HALF_UP);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testScoresAfterIteration4onRandomGraph0() throws FileNotFoundException {
        VertexScoringAlgorithm<Integer, BigDecimal> algorithm = new DAGPageRankCentrality<>(graph0, 4);

        Map<Integer, BigDecimal> expectedScores = new HashMap<>();

        expectedScores.put(0, new BigDecimal(2.783783783783783783783783783783783783783783783)); // 103/37
        expectedScores.put(1, new BigDecimal(0.216216216216216216216216216216216216216216216)); // 8/37
        expectedScores.put(2, new BigDecimal(1.391891891891891891891891891891891891891891891)); // 103/74
        expectedScores.put(3, new BigDecimal(1.5));// 103/74+ 4/37
        expectedScores.put(4, new BigDecimal(0.108108108108108108108108108108108108108108108)); // 4/37
        expectedScores.put(5, new BigDecimal(0.695945945945945945945945945945945945945945945)); // 103/148
        expectedScores.put(6, new BigDecimal(0.695945945945945945945945945945945945945945945)); // 103/148
        expectedScores.put(7, new BigDecimal(1.608108108108108108108108108108108108108108108)); // 103/74 + 8/37

        for (Integer v : graph0.vertexSet()) {
            BigDecimal expected = expectedScores.get(v).setScale(15, RoundingMode.HALF_UP);
            BigDecimal actual = algorithm.getVertexScore(v).setScale(15, RoundingMode.HALF_UP);
            assertEquals(expected, actual);
        }
    }

}
