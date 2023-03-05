package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class FoundAtEdge extends Edge {
    public FoundAtEdge() {
        super(EdgeType.FOUND_AT);
        this.nodeTypes = Arrays.asList(NodeType.Place, NodeType.Resource);
    }

    public static Edge getInstance() {
        return new FoundAtEdge();
    }
}
