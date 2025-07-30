package no.uib.mayarobbestad.dagcentrality.algorithms;

import java.util.Map;

import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;

/**
 * Degree centrality
 * 
 * Calculates the degree centrality of each vertex of a graph. The degree
 * centrality of a
 * vertex $v$ is given by $C_D=deg(v)$
 * 
 * The algorithm is based on the description of Freeman in this article:
 * Freeman, L. C. (1978). Centrality in social networks conceptual
 * clarification. Social networks, 1(3), 215-239.
 * For the directed
 */
public class DegreeCentrality implements VertexScoringAlgorithm {

    @Override
    public Map getScores() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getScores'");
    }

    @Override
    public Object getVertexScore(Object v) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getVertexScore'");
    }
}
