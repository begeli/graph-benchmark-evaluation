package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class CanUseEdge extends Edge {
    public CanUseEdge() {
        super(EdgeType.CAN_USE);
        this.nodeTypes = Arrays.asList(NodeType.Person, NodeType.Resource);
    }

    public static Edge getInstance() {
        return new CanUseEdge();
    }
}
