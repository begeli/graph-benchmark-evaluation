package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class SupportsEdge extends Edge {
    public SupportsEdge() {
        super(EdgeType.SUPPORTS);
        this.nodeTypes = Arrays.asList(NodeType.Company, NodeType.Project);
    }

    public static Edge getInstance() {
        return new SupportsEdge();
    }
}
