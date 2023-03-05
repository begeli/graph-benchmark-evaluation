package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class InBusinessWithEdge extends Edge {
    public InBusinessWithEdge() {
        super(EdgeType.IN_BUSINESS_WITH);
        this.nodeTypes = Arrays.asList(NodeType.Company, NodeType.Company);
    }

    public static Edge getInstance() {
        return new InBusinessWithEdge();
    }
}
