package ch.ethz.spcl.query;

import ch.ethz.spcl.edge.Edge;
import ch.ethz.spcl.edge.EdgeFactory;
import ch.ethz.spcl.node.NodeFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;

import java.util.Collections;
import java.util.Map;

public class AssocCountQuery extends Query {
    private Edge edge;

    public AssocCountQuery(JanusGraph graph, GraphTraversalSource source) {
        super(graph, source);
        edge = EdgeFactory.createEdge(NodeFactory.createNode(startVertexId).getNodeType());
    }

    public Map<Long, QueryResultSummary> execute() {
        long id = Thread.currentThread().getId();
        try {
            long startTime = System.nanoTime();
            source.V().has("id", startVertexId).both(edge.getEdgeType().toString()).count().next();
            long endTime = System.nanoTime();

            // assoc_count has id 4
            return Collections.singletonMap(id, new QueryResultSummary(4, endTime - startTime, 0));
        } catch (Exception e) {
            return Collections.singletonMap(id, new QueryResultSummary(4, -1, 1));
        }
    }
}
