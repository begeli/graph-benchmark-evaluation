package ch.ethz.spcl.query;

import ch.ethz.spcl.node.Node;
import ch.ethz.spcl.node.utils.NodeUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;

import java.util.Collections;
import java.util.Map;

public class InsertNodeQuery extends Query {
    private Node node;

    public InsertNodeQuery(JanusGraph graph, GraphTraversalSource source) {
        super(graph, source);
        node = NodeUtils.getRandomNode();
    }

    public  Map<Long, QueryResultSummary> execute() {
        long id = Thread.currentThread().getId();
        try {
            long startTime = System.nanoTime();
            GraphTraversal<Vertex, Vertex> vertex = source.addV(node.getNodeType().toString());
            Map<String, Object> properties = node.getRandomProperties();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                vertex = vertex.property(entry.getKey(), entry.getValue());
            }
            vertex.next();
            graph.tx().commit();
            long endTime = System.nanoTime();

            // obj_insert has id 1
            return Collections.singletonMap(id, new QueryResultSummary(1, endTime - startTime, 0));
        } catch (Exception e) {
            return Collections.singletonMap(id, new QueryResultSummary(1, -1, 1));
        }
    }
}
