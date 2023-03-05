package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class HasBranchesAtEdge extends Edge {
    public HasBranchesAtEdge() {
        super(EdgeType.HAS_BRANCHES_AT);
        this.nodeTypes = Arrays.asList(NodeType.Company, NodeType.Place);
    }

    public static Edge getInstance() {
        return new HasBranchesAtEdge();
    }
}
