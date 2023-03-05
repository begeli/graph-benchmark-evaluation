package ch.ethz.spcl.query;

import ch.ethz.spcl.edge.Edge;
import ch.ethz.spcl.edge.EdgeFactory;
import ch.ethz.spcl.node.NodeFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class AssocRangeQuery extends Query {
    private Edge edge;

    public AssocRangeQuery(JanusGraph graph, GraphTraversalSource source) {
        super(graph, source);
        edge = EdgeFactory.createEdge(NodeFactory.createNode(startVertexId).getNodeType());
    }

    public Map<Long, QueryResultSummary> execute() {
        long id = Thread.currentThread().getId();
        try {
            long startTime = System.nanoTime();
            GraphTraversal<Vertex, Vertex> neighbors
                    = source.V().has("id", startVertexId).both(edge.getEdgeType().toString());
            while (neighbors.hasNext()) {
                neighbors.next().id();
            }
            long endTime = System.nanoTime();

            // assoc_range has id 5
            return Collections.singletonMap(id, new QueryResultSummary(5, endTime - startTime, 0));
        } catch (Exception e) {
            return Collections.singletonMap(id, new QueryResultSummary(5, -1, 1));
        }
    }
}
