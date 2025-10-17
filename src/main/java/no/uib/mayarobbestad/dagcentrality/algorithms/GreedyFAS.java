package no.uib.mayarobbestad.dagcentrality.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import no.uib.mayarobbestad.dagcentrality.graph.GraphCopy;

public class GreedyFAS {

    /**
     * This algorithm finds a vertex sequence in a directed graph such that
     * the number of backwards directing edges is minimized. These edges are aprt
     * of the minimimal Feedback Arc Set. This method solves the linear arrangement
     * (LA) problem
     * This algorithm is based on the algorithm described in:
     * "A fast and effective heuristic for the feedback arc set problem"
     */
    public static List<Integer> GR(Graph<Integer, DefaultEdge> g) {
        List<Integer> s1 = new ArrayList<>();
        List<Integer> s2 = new ArrayList<>();
        List<Integer> s = new ArrayList<>();

        Graph<Integer, DefaultEdge> copy = GraphCopy.graphCopy(g);

        Set<Integer> sinks = new HashSet<>();
        Set<Integer> sources = new HashSet<>();

        for (Integer v : copy.vertexSet()) {
            if (copy.inDegreeOf(v) == 0)
                sources.add(v);
            if (copy.outDegreeOf(v) == 0)
                sinks.add(v);
        }

        while (!copy.vertexSet().isEmpty()) {

            while (!sinks.isEmpty()) {
                Integer v = sinks.iterator().next();
                sinks.remove(v);
                // if a sink vertex also has become a source vertex, remove this vertex from the
                // source list
                if (copy.inDegreeOf(v) == 0)
                    sources.remove(v);

                // list is reversed later
                s2.add(v);
                Set<DefaultEdge> incomingEdges = new HashSet<>(copy.incomingEdgesOf(v));

                for (DefaultEdge edge : incomingEdges) {
                    Integer u = copy.getEdgeSource(edge);
                    copy.removeEdge(edge);
                    // check if u is now a sink vertex
                    if (copy.outDegreeOf(u) == 0)
                        sinks.add(u);
                }
                copy.removeVertex(v);
            }

            while (!sources.isEmpty()) {
                Integer v = sources.iterator().next();
                sources.remove(v);
                s1.add(v);
                Set<DefaultEdge> outgoingEdges = new HashSet<>(copy.outgoingEdgesOf(v));
                for (DefaultEdge edge : outgoingEdges) {
                    Integer u = copy.getEdgeTarget(edge);
                    copy.removeEdge(edge);
                    // check if u is now a source vertex
                    if (copy.inDegreeOf(u) == 0)
                        sources.add(u);
                }
                copy.removeVertex(v);
            }

            // delta(u) = outdegree - indegree
            if (!copy.vertexSet().isEmpty()) {

                int max = Integer.MIN_VALUE;
                Integer bestVertex = 0;
                for (Integer v : copy.vertexSet()) {
                    int delta = copy.outDegreeOf(v) - copy.inDegreeOf(v);
                    if (delta > max) {
                        max = delta;
                        bestVertex = v;
                    }
                }

                s1.add(bestVertex);

                // update in and outdegrees
                // TODO: duplicate code??
                Set<DefaultEdge> incomingEdges = new HashSet<>(copy.incomingEdgesOf(bestVertex));
                for (DefaultEdge edge : incomingEdges) {
                    Integer u = copy.getEdgeSource(edge);
                    copy.removeEdge(edge);
                    // check if u is now a sink vertex
                    if (copy.outDegreeOf(u) == 0)
                        sinks.add(u);
                }

                Set<DefaultEdge> outgoingEdges = new HashSet<>(copy.outgoingEdgesOf(bestVertex));
                for (DefaultEdge edge : outgoingEdges) {
                    Integer u = copy.getEdgeTarget(edge);
                    copy.removeEdge(edge);
                    // check if u is now a source vertex
                    if (copy.inDegreeOf(u) == 0)
                        sources.add(u);
                }

                copy.removeVertex(bestVertex);
            }
        }

        int x = s1.size();
        Collections.reverse(s2);
        for (int i = 0; i < g.vertexSet().size(); i++) {
            if (i < x)
                s.add(s1.get(i));
            else
                s.add(s2.get(i - x));
        }

        return s;
    }

    /**
     * This method removes and returns the set of edges in the directed graph,
     * that make up a minimum Feedback Arc Set, F. The removal of these edges F in
     * the graph G result in G
     * becoming a Directed Acyclic Graph (DAG)
     * The is based on a greedy approach from "A fast and effective heuristic for
     * the feedback arc set problem"
     * This method calls the method GR, which finds the linear arrangement for which
     * the backward arcs make up a feedback arc set,
     * This is stated in this article: "Efficient Computation of Feedback Arc Set at
     * Web-Scale"
     * This method is based on my own ideas, perhaps there is a more efficient way
     * to
     * remove the edges from the graph
     */
    public static Set<DefaultEdge> removeCycleFromDirectedGraph(Graph<Integer, DefaultEdge> g) {
        Set<DefaultEdge> toRemove = new HashSet<>();
        List<Integer> A = GR(g);
        for (int i = 0; i < A.size(); i++) {
            for (DefaultEdge incoming : g.incomingEdgesOf(A.get(i))) {
                // if the edge points to the left
                // when ideally all edges should point to the right
                if (A.indexOf(g.getEdgeSource(incoming)) > i)
                    toRemove.add(incoming);
            }
        }
        g.removeAllEdges(toRemove);
        System.out.println("removed:" + toRemove);
        return toRemove;
    }

    /**
     * This algoritm checks if there is a cycle in the directed graph,
     * by performing dfs iteratively from each root node
     * 
     * @param <V>
     * @param <E>
     * @param g
     * @return
     */
    public static <V, E> boolean graphHasCycle(Graph<Integer, DefaultEdge> g) {
        boolean[] globalVisited = new boolean[g.vertexSet().size()];
        HashSet<Integer> roots = findAllRootVertices(g);
        // set of vertices visited in the current recusrive call
        Stack<Integer> toSearch = new Stack<>();
        if (roots.isEmpty())
            return true;

        for (Integer root : roots) {
            toSearch.push(root);

            boolean[] localVisited = new boolean[g.vertexSet().size()];
            while (!toSearch.isEmpty()) {
                Integer current = toSearch.pop();
                globalVisited[current] = true;
                localVisited[current] = true;
                for (DefaultEdge neighbourEdge : g.outgoingEdgesOf(current)) {
                    Integer neighbour = g.getEdgeTarget(neighbourEdge);
                    if (localVisited[neighbour])
                        return true;
                    // only continue the recursive call if the vertex has not been
                    // visited in this tree, or if it has not been visited by any other
                    // tree with any other root
                    if (!globalVisited[neighbour] && !localVisited[neighbour])
                        toSearch.push(neighbour);
                }
            }
        }
        return false;
    }

    private static <V, E> HashSet<Integer> findAllRootVertices(Graph<Integer, DefaultEdge> g) {
        HashSet<Integer> roots = new HashSet<>();
        for (Integer vertex : g.vertexSet()) {
            if (g.inDegreeOf(vertex) == 0)
                roots.add(vertex);
        }
        return roots;
    }
}
