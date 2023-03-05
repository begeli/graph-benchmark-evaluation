package ch.ethz.spcl.query;

import ch.ethz.spcl.query.utils.QueryUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Aggregate Query name might be a bit misleading.
 * In this query we execute a group of standard LinkBench queries
 * (So, we don't do any aggregation!)
 * Number of queries we execute is determined by the input parameter taskCount
 * */
public class AggregateQuery implements Callable<Map<Long, QueryResultSummary>> {
    private List<Query> queries;

    public AggregateQuery(int taskCount, List<Integer> weightPrefixSums, JanusGraph graph, GraphTraversalSource source) {
        queries = QueryUtils.generateQueries(taskCount, weightPrefixSums, graph, source);
    }

    @Override
    public Map<Long, QueryResultSummary> call() {
        long id = Thread.currentThread().getId();
        List<Map<Long, QueryResultSummary>> results = new ArrayList<>();
        for (Query query : queries) {
            results.add(query.execute());
        }

        long queryRuntimes = 0l;
        int failedQueryCount = 0;
        for (Map<Long, QueryResultSummary> result : results) {
            if (result.get(id).getExecutionTime() == -1) {
                failedQueryCount++;
            } else {
                queryRuntimes += result.get(id).getExecutionTime();
            }
        }

        return Collections.singletonMap(id, new QueryResultSummary(-1, queryRuntimes, failedQueryCount));
    }
}
