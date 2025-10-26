package no.uib.mayarobbestad.dagcentrality.graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomDagGenerator {

    /**
     * Assumes a topological ordering from 0-n,
     * only adds an edge between a random source to a random target
     * as long as the source vertex comes prior to the target vertex in the
     * topological ordering.
     * 
     * @param n
     * @param m
     * @throws IOException
     */
    public static void GenerateRandomDag(int n, int m, String folder) throws IOException {
        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adjacencyList.add(new ArrayList<>());
        }
        Random rand = new Random();
        int i = 0;
        ;
        if (m > Math.floor((n * (n - 1) / 2))) {
            throw new IllegalArgumentException(
                    "It is not possible to generate a DAG with " + m + " edges and " + n + "vertices.");
        }

        while (i < m) {
            int source = rand.nextInt(n);
            int target = rand.nextInt(source, n);
            // nos self loops allowed
            if (source == target) {
                continue;
            }
            // edge already exists
            if (adjacencyList.get(source).contains(target)) {
                continue;
            }
            adjacencyList.get(source).add(target);
            i++;
        }
        storeInGMLFormat(adjacencyList, n, m, folder);
    }

    private static void storeInGMLFormat(List<List<Integer>> adjacencyList, int n, int m, String folder)
            throws IOException {
        String path = folder + "random_dag_with_" + n + "_vertices" + m + "_edges.gml";
        File file = new File(path);
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write("graph [\n");
        writer.write(" directed 1\n");
        for (int i = 0; i < n; i++) {
            String node = " node [ id " + i + " ]\n";
            writer.write(node);
        }
        for (int source = 0; source < n; source++) {
            for (int j = 0; j < adjacencyList.get(source).size(); j++) {
                int target = adjacencyList.get(source).get(j);
                String edge = " edge [ source " + source + " target " + target + " ]\n";
                writer.write(edge);
            }
        }
        writer.write("]");
        writer.close();
    }
}
