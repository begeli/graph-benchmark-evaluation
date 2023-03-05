package ch.ethz.spcl.query;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;

import java.util.Collections;
import java.util.Map;

public class DeleteNodeQuery extends Query {

    public DeleteNodeQuery(JanusGraph graph, GraphTraversalSource source) {
        super(graph, source);
    }

    public Map<Long, QueryResultSummary> execute() {
        long id = Thread.currentThread().getId();
        try {
            long startTime = System.nanoTime();
            source.V().has("id", startVertexId).next().remove();
            graph.tx().commit();
            long endTime = System.nanoTime();

            // obj_delete has id 2
            return Collections.singletonMap(id, new QueryResultSummary(2, endTime - startTime, 0));
        } catch (Exception e) {
            return Collections.singletonMap(id, new QueryResultSummary(2, -1, 1));
        }
    }
}
