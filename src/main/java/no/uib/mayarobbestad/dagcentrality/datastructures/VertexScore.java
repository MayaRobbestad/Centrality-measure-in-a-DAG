package no.uib.mayarobbestad.dagcentrality.datastructures;

public class VertexScore implements Comparable {
    private String algorithm;
    private String graph;
    private Integer iteration;
    private Integer vertex;
    private Double score;

    public VertexScore(String algorithm, String graph, Integer iteration, Integer vertex, Double score) {
        this.algorithm = algorithm;
        this.graph = graph;
        this.iteration = iteration;
        this.vertex = vertex;
        this.score = score;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getGraph() {
        return graph;
    }

    public Integer getIteration() {
        return iteration;
    }

    public Integer getVertex() {
        return vertex;
    }

    public Double getScore() {
        return score;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        VertexScore entry = (VertexScore) obj;
        return algorithm.equals(entry.algorithm) && graph.equals(entry.graph) && iteration == entry.iteration
                && vertex == entry.vertex;
    }

    @Override
    public int compareTo(Object obj) {
        VertexScore other = (VertexScore) obj;
        if (this.score < other.score) {
            return -1;
        }
        if (this.score > other.score)
            return 1;
        return 0; // then they must be equal
    }

    @Override
    public String toString() {
        return "Algorithm = " + algorithm + " Graph = " + graph + " Iteration =" + iteration + " Vertex = " + vertex
                + " Score = " + score;
    }

}
