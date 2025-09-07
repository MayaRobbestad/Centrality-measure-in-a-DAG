package no.uib.mayarobbestad.dagcentrality.datastructures;

public class VertexCentralityPair<V> implements Comparable {

    private V vertex;
    private Double score;

    public VertexCentralityPair(V vertex, Double score) {
        this.vertex = vertex;
        this.score = score;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        VertexCentralityPair<V> pair = (VertexCentralityPair<V>) obj;
        return vertex.equals(pair.vertex);
    }

    @Override
    public int compareTo(Object obj) {

        VertexCentralityPair<V> other = (VertexCentralityPair<V>) obj;

        if (this.score < other.score) {
            return -1;
        }
        if (this.score > other.score)
            return 1;

        return 0; // then they must be equal

    }

}
