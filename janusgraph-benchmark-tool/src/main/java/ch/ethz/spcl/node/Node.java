package ch.ethz.spcl.node;

import ch.ethz.spcl.query.QueryResultSummary;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;

import java.util.Map;

public abstract class Node {
    public static final long GLOBAL_MIN_ID = CompanyNode.getMinValue();
    public static final long GLOBAL_MAX_ID = ResourceNode.getMaxValue();
    protected String idField;
    protected NodeType nodeType;

    public Node(String idField) {
        this.idField = idField;
    }

    public NodeType getNodeType() {
        return this.nodeType;
    }

    public String getIdField() {
        return this.idField;
    }

    public static long getMinValue() {
        return GLOBAL_MIN_ID;
    }
    public static long getMaxValue() {
        return GLOBAL_MAX_ID;
    }
    public abstract Map<String, Object> getRandomProperties();
    public abstract Map<Long, QueryResultSummary> updateProperties(
            JanusGraph graph,
            GraphTraversalSource source,
            long vertexId
    );
}
