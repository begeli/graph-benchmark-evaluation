package ch.ethz.spcl.query;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalMetrics;
import org.janusgraph.core.JanusGraph;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GetNodeQuery extends Query {

    public GetNodeQuery(JanusGraph graph, GraphTraversalSource source) {
        super(graph, source);
    }

    public Map<Long, QueryResultSummary> execute() {
        long id = Thread.currentThread().getId();
        try {
            long startTime = System.nanoTime();
            // TraversalMetrics metrics = source.V().has("id", startVertexId).profile().next();
            source.V().has("id", startVertexId).next();
            long endTime = System.nanoTime();

            // obj_get query has id 0
            // return Collections.singletonMap(id, new QueryResultSummary(0, metrics.getDuration(TimeUnit.MICROSECONDS), 0));
            return Collections.singletonMap(id, new QueryResultSummary(0, endTime - startTime, 0));
        } catch (Exception e) {
            return Collections.singletonMap(id, new QueryResultSummary(0, -1, 1));
        }
    }
}
