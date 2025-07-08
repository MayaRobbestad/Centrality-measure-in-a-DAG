package algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import no.uib.mayarobbestad.dagcentrality.algorithms.GreedyFAS;
import no.uib.mayarobbestad.dagcentrality.graph.GraphBuilder;

/**
 * Took inspiration from H24 INF102 sem2 tests
 */
public class GreedyFASTest {

    GreedyFAS fas;
    Graph<Integer, DefaultEdge> g;
    List<Integer> output;
    ArrayList<Integer> answers;

    private void generateTestCase(int i) throws IOException {
        g = new GraphBuilder().readGraphFromInputFile("data/synthetic/directed/fas" + i + ".in", true);
        Scanner sc = new Scanner(new FileReader(new File("algorithmsOutputAnswers/FAS/fas" + i + ".ans")));
        answers = new ArrayList<>();
        while (sc.hasNextLine()) {
            answers.add(sc.nextInt());
        }
    }

    // @Test
    // void test0() throws IOException {
    // generateTestCase(0);
    // output = GreedyFAS.GR(g);
    // for (int i = 0; i < output.size(); i++) {
    // assertEquals(answers.get(i), output.get(i));
    // }
    // }

    @Test
    void test1() throws IOException {
        generateTestCase(1);
        output = GreedyFAS.GR(g);
        for (int i = 0; i < output.size(); i++) {
            assertEquals(answers.get(i), output.get(i));
        }
        // Set<DefaultEdge> F = GreedyFAS.removeCycleFromDirectedGraph(g);
        // int source = 3;
        // int target = 4;
        // assertEquals("(" + source + " : " + target + ")",
        // F.iterator().next().toString());

    }

}
