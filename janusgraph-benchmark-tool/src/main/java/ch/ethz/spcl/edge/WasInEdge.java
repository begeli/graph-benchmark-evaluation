package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class WasInEdge extends Edge {
    public WasInEdge() {
        super(EdgeType.WAS_IN);
        this.nodeTypes = Arrays.asList(NodeType.Person, NodeType.Place);
    }

    public static Edge getInstance() {
        return new WasInEdge();
    }
}
