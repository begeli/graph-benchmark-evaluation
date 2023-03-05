package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class CanBeUsedWithEdge extends Edge {
    public CanBeUsedWithEdge() {
        super(EdgeType.CAN_BE_USED_WITH);
        this.nodeTypes = Arrays.asList(NodeType.Resource, NodeType.Resource);
    }

    public static Edge getInstance() {
        return new CanBeUsedWithEdge();
    }
}
