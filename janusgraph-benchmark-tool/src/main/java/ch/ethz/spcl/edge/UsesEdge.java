package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class UsesEdge extends Edge {
    public UsesEdge() {
        super(EdgeType.USES);
        this.nodeTypes = Arrays.asList(NodeType.Company, NodeType.Resource);
    }

    public static Edge getInstance() {
        return new UsesEdge();
    }
}
