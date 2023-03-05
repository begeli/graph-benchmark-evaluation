package ch.ethz.spcl.query.utils;

import ch.ethz.spcl.query.AggregateQuery;
import ch.ethz.spcl.query.Query;
import ch.ethz.spcl.query.QueryFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;

import java.util.ArrayList;
import java.util.List;

public class QueryUtils {
    public static List<AggregateQuery> generateAggregateQueries(
            int taskCount,
            int threadCount,
            List<Integer> weightPrefixSums,
            JanusGraph graph,
            GraphTraversalSource source
    ) {
        List<AggregateQuery> queries = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            queries.add(new AggregateQuery(taskCount, weightPrefixSums, graph, source));
        }

        return queries;
    }
    public static List<Query> generateQueries(
            int queryCount,
            List<Integer> weightPrefixSums,
            JanusGraph graph,
            GraphTraversalSource source
    ) {
        List<Query> queries = new ArrayList<>();
        for (long i = 0; i < (long) queryCount; i++) {
            Query query = QueryFactory.createQuery(weightPrefixSums, graph, source);
            queries.add(query);
        }

        return queries;
    }
}
