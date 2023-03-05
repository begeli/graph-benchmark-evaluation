package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class WorksAtEdge extends Edge {
    public WorksAtEdge() {
        super(EdgeType.WORKS_AT);
        this.nodeTypes = Arrays.asList(NodeType.Person, NodeType.Company);
    }

    public static Edge getInstance() {
        return new WorksAtEdge();
    }
}
