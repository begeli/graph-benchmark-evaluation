package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class IsPartOfEdge extends Edge {
    public IsPartOfEdge() {
        super(EdgeType.IS_PART_OF);
        this.nodeTypes = Arrays.asList(NodeType.Person, NodeType.Project);
    }

    public static Edge getInstance() {
        return new IsPartOfEdge();
    }
}
