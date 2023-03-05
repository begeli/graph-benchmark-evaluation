package ch.ethz.spcl.query;

import ch.ethz.spcl.edge.Edge;
import ch.ethz.spcl.edge.EdgeFactory;
import ch.ethz.spcl.node.Node;
import ch.ethz.spcl.node.NodeFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class AssocInsertQuery extends Query {
    private int endVertexId;
    private Edge edge;

    public AssocInsertQuery(JanusGraph graph, GraphTraversalSource source) {
        super(graph, source);
        endVertexId = ThreadLocalRandom.current().nextInt(0, (int) Node.GLOBAL_MAX_ID + 1);
        Node sourceVertex = NodeFactory.createNode(startVertexId);
        Node destinationVertex = NodeFactory.createNode(endVertexId);
        edge = EdgeFactory.createEdge(sourceVertex.getNodeType(), destinationVertex.getNodeType());
    }

    public Map<Long, QueryResultSummary> execute() {
        long id = Thread.currentThread().getId();
        try {
            long startTime = System.nanoTime();
            Vertex destination = source.V().has("id", endVertexId).next();
            source.V()
                    .has("id", startVertexId)
                    .addE(edge.getEdgeType().toString())
                    .to(destination)
                    .next();
            graph.tx().commit();
            long endTime = System.nanoTime();

            // assoc_insert has id 6
            return Collections.singletonMap(id, new QueryResultSummary(6, endTime - startTime, 0));
        } catch (Exception e) {
            return Collections.singletonMap(id, new QueryResultSummary(6, -1, 1));
        }
    }
}
