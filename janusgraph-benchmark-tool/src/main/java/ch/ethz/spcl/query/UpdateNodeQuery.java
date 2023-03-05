package ch.ethz.spcl.query;

import ch.ethz.spcl.node.Node;
import ch.ethz.spcl.node.NodeFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;

import java.util.Map;

public class UpdateNodeQuery extends Query {
    private Node node;

    public UpdateNodeQuery(JanusGraph graph, GraphTraversalSource source) {
        super(graph, source);
        node = NodeFactory.createNode(startVertexId);
    }

    public  Map<Long, QueryResultSummary> execute() {
        return node.updateProperties(graph, source, startVertexId);
    }
}
