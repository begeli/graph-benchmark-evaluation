package ch.ethz.spcl.edge;

import ch.ethz.spcl.node.NodeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EdgeFactory {
    public static Edge createEdge(NodeType nodeType) {
        List<Edge> allEdges = getAllEdgeInstances();

        List<Edge> candidateEdges = new ArrayList<>();
        for (Edge edge : allEdges) {
            if (edge.getNodeTypes().contains(nodeType)) {
                candidateEdges.add(edge);
            }
        }

        // Each node type has 5 edge types it can have
        int selectedEdgeNum = ThreadLocalRandom.current().nextInt(0, 5);
        return candidateEdges.get(selectedEdgeNum);
    }

    public static Edge createEdge(NodeType nodeType1, NodeType nodeType2) {
        List<Edge> allEdges = getAllEdgeInstances();
        for (Edge edge : allEdges) {
            if (edge.getNodeTypes().containsAll(Arrays.asList(nodeType1, nodeType2))) {
                return edge;
            }
        }
        throw new IllegalArgumentException(
                String.format("There is no possible edge between %s and %s",
                        nodeType1.toString(),
                        nodeType2.toString())
        );
    }

    private static List<Edge> getAllEdgeInstances() {
        List<Edge> allEdges = Arrays.asList(
                CanBeUsedWithEdge.getInstance(),
                CanUseEdge.getInstance(),
                FoundAtEdge.getInstance(),
                HasBranchesAtEdge.getInstance(),
                ImpactsEdge.getInstance(),
                InBusinessWithEdge.getInstance(),
                InfluencesEdge.getInstance(),
                InVicinityOfEdge.getInstance(),
                IsPartOfEdge.getInstance(),
                KnowsEdge.getInstance(),
                NeedsEdge.getInstance(),
                SupportsEdge.getInstance(),
                UsesEdge.getInstance(),
                WasInEdge.getInstance(),
                WorksAtEdge.getInstance()
        );

        return allEdges;
    }
}
