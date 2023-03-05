package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class InVicinityOfEdge extends Edge {
    public InVicinityOfEdge() {
        super(EdgeType.IN_VICINITY_OF);
        this.nodeTypes = Arrays.asList(NodeType.Place, NodeType.Place);
    }

    public static Edge getInstance() {
        return new InVicinityOfEdge();
    }
}
