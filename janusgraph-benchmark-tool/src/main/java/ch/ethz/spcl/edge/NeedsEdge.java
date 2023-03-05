package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class NeedsEdge extends Edge {
    public NeedsEdge() {
        super(EdgeType.NEEDS);
        this.nodeTypes = Arrays.asList(NodeType.Project, NodeType.Resource);
    }

    public static Edge getInstance() {
        return new NeedsEdge();
    }
}
