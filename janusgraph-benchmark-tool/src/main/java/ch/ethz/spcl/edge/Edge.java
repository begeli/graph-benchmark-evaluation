package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.List;

public abstract class Edge {
    protected EdgeType type;
    protected List<NodeType> nodeTypes;

    public Edge(EdgeType type) {
        this.type = type;
    }

    public EdgeType getEdgeType() {
        return type;
    }

    public List<NodeType> getNodeTypes() {
        return nodeTypes;
    }
}
