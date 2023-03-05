package com.nono;

import com.nono.utils.Pair;
import org.neo4j.driver.*;
import org.neo4j.driver.summary.ResultSummary;
import org.slf4j.helpers.NOPLogger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutionController {

    public ExecutionController() {
    }

    static int objGetNo;
    static int objInsertNo;
    static int objDeleteNo;
    static int objUpdateNo;
    static int assocCountNo;
    static int assocRangeNo;
    static int assocInsertNo;
    static int tasksNumberNo;
    static long id;
    static long threadsPerNode;

    public static void runBenchmark(
            String server,
            String user,
            String password,
            int tasksNumber,
            int threadsNumber,
            long startTime,
            String database,
            int objGet,
            int objInsert,
            int objDelete,
            int objUpdate,
            int assocCount,
            int assocRange,
            int assocInsert,
            int nodeId,
            int threadPerNode,
            boolean isThroughput
        ) {
        id = nodeId;
        threadsPerNode = threadPerNode;
        tasksNumberNo = tasksNumber;

        Driver driver = GraphDatabase.driver(
                String.format("neo4j://%S", server),
                AuthTokens.basic(user, password)
        );

        try {
            driver.verifyConnectivity();
        } catch (RuntimeException e) {
            System.out.println("Failed to connect to Neo4j server...");
            System.out.println(e.getMessage());
            return;
        }

        if (startTime > 0) {
            while (System.currentTimeMillis() < startTime) {}
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threadsNumber);
        List<Query> tasksToExecute = new ArrayList<>();

        /* Dummy queries */
        for (int i = 0; i < tasksNumberNo / 20; i++) {
            tasksToExecute.add(Query.GetNodesQuery(driver, database));
            tasksToExecute.add(Query.UpdateNodeQuery(driver, database));
            tasksToExecute.add(Query.CountAssocQuery(driver, database));
            tasksToExecute.add(Query.RangeAssocQuery(driver, database));
        }

        try {
            executorService.invokeAll(tasksToExecute);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        tasksToExecute = new ArrayList<>();
        for (int i = 0; i < tasksNumber * threadsNumber; i++) {
            tasksToExecute.add(
                    createRandomQuery(
                            driver,
                            database,
                            Arrays.asList(
                                objGet,
                                objInsert,
                                objDelete,
                                objUpdate,
                                assocCount,
                                assocRange,
                                assocInsert
                            )
                    )
            );
        }

        /* Permute to get random execution order. */
        Collections.shuffle(tasksToExecute);

        if (isThroughput) {
            getThroughputResults(executorService, tasksToExecute, threadsNumber);
        } else {
            getLatencyResults(executorService, tasksToExecute, threadsNumber);
        }
        /*try {
            List<Future<Map<Pair<Long, Integer>, ResultSummary>>> results = executorService.invokeAll(tasksToExecute);
            executorService.shutdown();

            Query.readSessions.forEach((id, session) -> session.close());
            Query.readSessions.clear();
            Query.writeSessions.forEach((id, session) -> session.close());
            Query.writeSessions.clear();

            ConcurrentHashMap<Long, AtomicLong> executionTimes = new ConcurrentHashMap<>();
            // Keep a global accumulator for all queries
            //ConcurrentHashMap<Long, AtomicInteger> queryCounts = new ConcurrentHashMap<>();
            AtomicInteger queryCounts = new AtomicInteger(0);
            results.forEach(
                    result -> {
                        try {
                            Map<Pair<Long, Integer>, ResultSummary> futureResult = result.get();
                            Long threadId = id * threadsPerNode
                                    +futureResult.keySet().stream().findAny().get().getKey() % threadsNumber;
                            executionTimes.putIfAbsent(threadId, new AtomicLong(0));

                            ResultSummary resultSummary = futureResult.values().stream().findAny().get();
                            long executionTimeDelta = resultSummary.resultConsumedAfter(TimeUnit.MILLISECONDS)
                                    + resultSummary.resultAvailableAfter(TimeUnit.MILLISECONDS);
                            executionTimes.get(threadId).addAndGet(executionTimeDelta);
                            queryCounts.incrementAndGet();
                        } catch (Exception e) {
                            //System.out.println(e.getMessage());
                        }
                    }
            );

            for (Long threadId : executionTimes.keySet()) {
                System.out.println(threadId + "," + tasksNumberNo + "," + (tasksNumberNo - queryCounts.get() / threadsNumber) + "," + executionTimes.get(threadId).get() + ",0");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }*/
    }

    private static void getThroughputResults(ExecutorService executorService, List<Query> tasksToExecute, int threadsNumber) {
        try {
            List<Future<Map<Pair<Long, Integer>, ResultSummary>>> results = executorService.invokeAll(tasksToExecute);
            executorService.shutdown();

            Query.readSessions.forEach((id, session) -> session.close());
            Query.readSessions.clear();
            Query.writeSessions.forEach((id, session) -> session.close());
            Query.writeSessions.clear();

            ConcurrentHashMap<Long, AtomicLong> executionTimes = new ConcurrentHashMap<>();
            // Keep a global accumulator for all queries
            //ConcurrentHashMap<Long, AtomicInteger> queryCounts = new ConcurrentHashMap<>();
            AtomicInteger queryCounts = new AtomicInteger(0);
            results.forEach(
                    result -> {
                        try {
                            Map<Pair<Long, Integer>, ResultSummary> futureResult = result.get();
                            Long threadId = id * threadsPerNode
                                    +futureResult.keySet().stream().findAny().get().getKey() % threadsNumber;
                            executionTimes.putIfAbsent(threadId, new AtomicLong(0));

                            ResultSummary resultSummary = futureResult.values().stream().findAny().get();
                            long executionTimeDelta = resultSummary.resultConsumedAfter(TimeUnit.MILLISECONDS)
                                    + resultSummary.resultAvailableAfter(TimeUnit.MILLISECONDS);
                            executionTimes.get(threadId).addAndGet(executionTimeDelta);
                            queryCounts.incrementAndGet();
                        } catch (Exception e) {
                            //System.out.println(e.getMessage());
                        }
                    }
            );

            for (Long threadId : executionTimes.keySet()) {
                System.out.println(threadId + "," + tasksNumberNo + "," + (tasksNumberNo - queryCounts.get() / threadsNumber) + "," + executionTimes.get(threadId).get() + ",0");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private static void getLatencyResults(ExecutorService executorService, List<Query> tasksToExecute, int threadsNumber) {
        try {
            List<Future<Map<Pair<Long, Integer>, ResultSummary>>> results = executorService.invokeAll(tasksToExecute);
            executorService.shutdown();

            Query.readSessions.forEach((id, session) -> session.close());
            Query.readSessions.clear();
            Query.writeSessions.forEach((id, session) -> session.close());
            Query.writeSessions.clear();

            // Keep a global accumulator for all queries
            //ConcurrentHashMap<Long, AtomicInteger> queryCounts = new ConcurrentHashMap<>();
            results.forEach(
                    result -> {
                        try {
                            Map<Pair<Long, Integer>, ResultSummary> futureResult = result.get();
                            Pair<Long, Integer> threadIdQueryTypePair = futureResult.keySet().stream().findAny().get();
                            Long threadId = id * threadsPerNode
                                    + threadIdQueryTypePair.getKey() % threadsNumber;
                            Integer queryType = threadIdQueryTypePair.getValue();
                            ResultSummary resultSummary = futureResult.values().stream().findAny().get();
                            long executionTimeDelta = resultSummary.resultConsumedAfter(TimeUnit.MILLISECONDS)
                                    + resultSummary.resultAvailableAfter(TimeUnit.MILLISECONDS);

                            System.out.println(threadId + "," + queryType + "," + executionTimeDelta);
                        } catch (Exception e) {
                            //System.out.println(e.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private static Query createRandomQuery(Driver driver, String database, List<Integer> weights) {
        Random random = new Random();
        int weight = random.nextInt(weights.get(weights.size() - 1));

        int queryIndex;
        for (queryIndex = 0; queryIndex < weights.size(); queryIndex++) {
            if (weight < weights.get(queryIndex)) {
                break;
            }
        }

        switch (queryIndex) {
            case 0:
                return Query.GetNodesQuery(driver, database);
            case 1:
                return Query.InsertNodeQuery(driver, database);
            case 2:
                return Query.DeleteNodeQuery(driver, database);
            case 3:
                return Query.UpdateNodeQuery(driver, database);
            case 4:
                return Query.CountAssocQuery(driver, database);
            case 5:
                return Query.RangeAssocQuery(driver, database);
            case 6:
                return Query.InsertAssocQuery(driver, database);
            default:
                throw new IllegalArgumentException("Unknown query type.");
        }
    }
}
