package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class KnowsEdge extends Edge {
    public KnowsEdge() {
        super(EdgeType.KNOWS);
        this.nodeTypes = Arrays.asList(NodeType.Person, NodeType.Person);
    }

    public static Edge getInstance() {
        return new KnowsEdge();
    }
}
