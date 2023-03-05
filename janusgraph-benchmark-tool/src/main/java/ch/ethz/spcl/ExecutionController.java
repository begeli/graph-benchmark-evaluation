package ch.ethz.spcl;

import ch.ethz.spcl.query.*;
import ch.ethz.spcl.query.utils.QueryUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutionController {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionController.class);
    private static final int WARM_UP_QUERY_COUNT = 100;
    private static long threadCountNo;
    private static long taskCountNo;
    private static long nodeId;
    private static long threadsPerNode;
    public static void runBenchmark(
            JanusGraph graph,
            GraphTraversalSource source,
            int taskCount,
            int threadCount,
            List<Integer> weightPrefixSums,
            boolean isAggregate,
            int id,
            int threadPerNode
    ) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        threadCountNo = threadCount;
        taskCountNo = taskCount;
        nodeId = id;
        threadsPerNode = threadPerNode;

        warmUp(executorService, graph, source);

        if (isAggregate) {
            // Create a single callable Aggregate query per thread
            // this aggregate query will execute taskCount amount of queries
            List<AggregateQuery> queries = QueryUtils.generateAggregateQueries(
                    taskCount,
                    threadCount,
                    weightPrefixSums,
                    graph,
                    source
            );
            runAggregatedLatencyBenchmark(queries, executorService);
        } else {
            List<Query> queries = QueryUtils.generateQueries(
                    taskCount * threadCount,
                    weightPrefixSums,
                    graph,
                    source
            );
            runQueryLatencyBenchmark(queries, executorService);
        }

        executorService.shutdown();
    }

    private static void runAggregatedLatencyBenchmark(
            List<AggregateQuery> queries,
            ExecutorService executorService
    ) {
        try {
            List<Future<Map<Long, QueryResultSummary>>> results = executorService.invokeAll(queries);

            //System.out.println("rank,num_queries,failed_queries,queries_time,barrier_time");
            results.forEach(
                result -> {
                    try {
                        Map<Long, QueryResultSummary> futureResult = result.get();
                        Long threadId = nodeId * threadsPerNode
                                + futureResult.keySet().stream().findAny().get() % threadCountNo;
                        QueryResultSummary resultSummary = futureResult.values().stream().findAny().get();
                        System.out.printf(
                                "%d,%d,%d,%f,0",
                                threadId,
                                taskCountNo,
                                resultSummary.getFailedQueryCount(),
                                resultSummary.getExecutionTime() / 1000.0
                        );
                        System.out.print(System.lineSeparator());
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                    }
                }
            );
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void runQueryLatencyBenchmark(
            List<Query> queries,
            ExecutorService executorService
    ) {
        try {
            List<Future<Map<Long, QueryResultSummary>>> results = executorService.invokeAll(queries);
            //System.out.println("rank,query_num,querie_lat");
            results.forEach(
                result -> {
                    try {
                        Map<Long, QueryResultSummary> futureResult = result.get();
                        Long threadId = nodeId * threadsPerNode
                                + futureResult.keySet().stream().findAny().get() % threadCountNo;
                        QueryResultSummary resultSummary = futureResult.values().stream().findAny().get();
                        if (resultSummary.getFailedQueryCount() == 1) {
                            return;
                        }
                        System.out.printf(
                                "%d,%d,%f",
                                threadId,
                                resultSummary.getQueryId(),
                                (resultSummary.getExecutionTime() / 1000.0)
                        );
                        System.out.print(System.lineSeparator());
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                }
            );
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static void warmUp(
            ExecutorService executorService,
            JanusGraph graph,
            GraphTraversalSource source
    ) {
        List<Callable<Map<Long, QueryResultSummary>>> warmUpQueries = new ArrayList<>();
        for (int i = 0; i < WARM_UP_QUERY_COUNT / 4; i++) {
            warmUpQueries.add(new GetNodeQuery(graph, source));
            warmUpQueries.add(new UpdateNodeQuery(graph, source));
            warmUpQueries.add(new AssocCountQuery(graph, source));
            warmUpQueries.add(new AssocRangeQuery(graph, source));
        }

        try {
            executorService.invokeAll(warmUpQueries);
        } catch (Exception e) {
            LOG.error("Warm up failed...");
            LOG.error(e.getMessage());
        }
        LOG.info("Warm up completed...");
    }
}
