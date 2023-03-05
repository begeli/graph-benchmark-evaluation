package ch.ethz.spcl.query;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;

import java.util.List;
import java.util.Random;

public class QueryFactory {
    public static Query createQuery(
            List<Integer> weightPrefixSums,
            JanusGraph graph,
            GraphTraversalSource source
    ) {
        Random random = new Random();
        int weight = random.nextInt(weightPrefixSums.get(weightPrefixSums.size() - 1));

        int queryIndex;
        for (queryIndex = 0; queryIndex < weightPrefixSums.size(); queryIndex++) {
            if (weight < weightPrefixSums.get(queryIndex)) {
                break;
            }
        }

        switch (queryIndex) {
            case 0:
                return new GetNodeQuery(graph, source);
            case 1:
                return new InsertNodeQuery(graph, source);
            case 2:
                return new DeleteNodeQuery(graph, source);
            case 3:
                return new UpdateNodeQuery(graph, source);
            case 4:
                return new AssocCountQuery(graph, source);
            case 5:
                return new AssocRangeQuery(graph, source);
            case 6:
                return new AssocInsertQuery(graph, source);
            default:
                throw new IllegalArgumentException("Unknown query type.");
        }
    }
}
