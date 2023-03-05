package ch.ethz.spcl.query;

import ch.ethz.spcl.node.Node;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Query implements Callable<Map<Long, QueryResultSummary>> {
    protected JanusGraph graph;
    protected GraphTraversalSource source;
    protected long startVertexId;

    public Query(JanusGraph graph, GraphTraversalSource source) {
        this.graph = graph;
        this.source = source;
        this.startVertexId = ThreadLocalRandom
                .current()
                .nextLong(0, Node.GLOBAL_MAX_ID + 1);
    }

    public abstract Map<Long, QueryResultSummary> execute();

    public Map<Long, QueryResultSummary> call() {
        return execute();
    }
}
