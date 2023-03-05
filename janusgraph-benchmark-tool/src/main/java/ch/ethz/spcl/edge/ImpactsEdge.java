package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.Arrays;

public class ImpactsEdge extends Edge {
    public ImpactsEdge() {
        super(EdgeType.IMPACTS);
        this.nodeTypes = Arrays.asList(NodeType.Place, NodeType.Project);
    }

    public static Edge getInstance() {
        return new ImpactsEdge();
    }
}
