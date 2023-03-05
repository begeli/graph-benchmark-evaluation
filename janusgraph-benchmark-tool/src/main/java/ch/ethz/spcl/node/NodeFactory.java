package ch.ethz.spcl.node;

public class NodeFactory {
    public NodeFactory() {}

    public static Node createNode(long id) {
        if (CompanyNode.getMinValue() <= id && id <= CompanyNode.getMaxValue()) {
            return new CompanyNode();
        } else if (PersonNode.getMinValue() <= id && id <= PersonNode.getMaxValue()) {
            return new PersonNode();
        } else if (PlaceNode.getMinValue() <= id && id <= PlaceNode.getMaxValue()) {
            return new PlaceNode();
        } else if (ProjectNode.getMinValue() <= id && id <= ProjectNode.getMaxValue()) {
            return new ProjectNode();
        } else if (ResourceNode.getMinValue() <= id && id <= ResourceNode.getMaxValue()) {
            return new ResourceNode();
        } else {
            throw new IllegalArgumentException("Given ID is out of range.");
        }
    }
}
