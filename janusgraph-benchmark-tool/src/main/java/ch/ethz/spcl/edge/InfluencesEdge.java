package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class InfluencesEdge extends Edge {
    public InfluencesEdge() {
        super(EdgeType.INFLUENCES);
        this.nodeTypes = Arrays.asList(NodeType.Project, NodeType.Project);
    }

    public static Edge getInstance() {
        return new InfluencesEdge();
    }
}
