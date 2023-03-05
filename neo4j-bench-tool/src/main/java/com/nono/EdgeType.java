package com.nono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.nono.NodeType.*;

public enum EdgeType {

    CAN_BE_USED_WITH("CAN_BE_USED_WITH", Arrays.asList(RESOURCE, RESOURCE)),
    CAN_USE("CAN_USE", Arrays.asList(PERSON, RESOURCE)),
    FOUND_AT("FOUND_AT", Arrays.asList(PLACE, RESOURCE)),
    HAS_BRANCHES_AT("HAS_BRANCHES_AT", Arrays.asList(COMPANY, PLACE)),
    IN_BUSINESS_WITH("IN_BUSINESS_WITH", Arrays.asList(COMPANY, COMPANY)),
    INFLUENCES("INFLUENCES", Arrays.asList(PROJECT, PROJECT)),
    IN_VICINITY_OF("IN_VICINITY_OF", Arrays.asList(PLACE, PLACE)),
    IS_PART_OF("IS_PART_OF", Arrays.asList(PERSON, PROJECT)),
    KNOWS("KNOWS", Arrays.asList(PERSON, PERSON)),
    NEEDS("NEEDS", Arrays.asList(PROJECT, RESOURCE)),
    SUPPORTS("SUPPORTS", Arrays.asList(COMPANY, PROJECT)),
    USES("USES", Arrays.asList(COMPANY, RESOURCE)),
    WAS_IN("WAS_IN", Arrays.asList(PERSON, PLACE)),
    WORKS_AT("WORKS_AT", Arrays.asList(PERSON, COMPANY)),
    IMPACTS("IMPACTS", Arrays.asList(PLACE, PROJECT));

    private String edgeType;
    private List<NodeType> nodeTypes;


    EdgeType(String edgeType, List<NodeType> nodeTypes) {
        this.edgeType = edgeType;
        this.nodeTypes = nodeTypes;
    }

    public String getEdgeType() {
        return edgeType;
    }

    public List<NodeType> getNodeTypes() {
        return nodeTypes;
    }

    public static EdgeType getForNodeType(NodeType nodeType) {
        List<EdgeType> edgeTypes = new ArrayList<>();
        for (EdgeType edgeType : EdgeType.values()) {
            if (edgeType.getNodeTypes().contains(nodeType)) {
                edgeTypes.add(edgeType);
            }
        }
        int randomNum = ThreadLocalRandom.current().nextInt(0, 5);
        return edgeTypes.get(randomNum);
    }

    public static EdgeType getForNodeTypes(NodeType nodeType, NodeType nodeType2) {
        for (EdgeType edgeType : EdgeType.values()) {
            if (
                    (edgeType.getNodeTypes().get(0) == nodeType && edgeType.getNodeTypes().get(1) == nodeType2) ||
                    (edgeType.getNodeTypes().get(1) == nodeType && edgeType.getNodeTypes().get(0) == nodeType2)
            ) {
                return edgeType;
            }
        }
        throw new RuntimeException("Not possible to find proper edge!");
    }
}
