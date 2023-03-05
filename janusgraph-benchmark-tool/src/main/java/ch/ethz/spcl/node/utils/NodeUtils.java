package ch.ethz.spcl.node.utils;

import ch.ethz.spcl.node.*;

import java.util.concurrent.ThreadLocalRandom;

public class NodeUtils {
    public static Node getRandomNode() {
        int randomType = ThreadLocalRandom.current().nextInt(0, 5);
        switch (randomType) {
            case 0:
                return new CompanyNode();
            case 1:
                return new PersonNode();
            case 2:
                return new PlaceNode();
            case 3:
                return new ProjectNode();
            case 4:
                return new ResourceNode();
            default:
                throw new RuntimeException(String.format("Not possible to generate range for %d", randomType));
        }
    }
}
