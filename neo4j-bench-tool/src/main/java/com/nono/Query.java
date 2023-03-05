package com.nono;

import com.nono.utils.Pair;
import org.apache.commons.lang3.RandomStringUtils;
import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.summary.ResultSummary;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Query implements Callable<Map<Pair<Long, Integer>, ResultSummary>> {

    boolean write;
    int queryType;
    String queryToExecute;

    /* Needed for Session creation. */
    Driver driver;
    String database;

    static ConcurrentHashMap<Long, Session> readSessions;
    static ConcurrentHashMap<Long, Session> writeSessions;

    static {
        readSessions = new ConcurrentHashMap<>();
        writeSessions = new ConcurrentHashMap<>();
    }

    public Query(boolean write, int queryType, String queryToExecute, Driver driver, String database) {
        this.write = write;
        this.queryType = queryType;
        this.queryToExecute = queryToExecute;
        this.driver = driver;
        this.database = database;
    }

    @Override
    public Map<Pair<Long, Integer>, ResultSummary> call() throws Exception {
        ConcurrentHashMap<Long, Session> sessions = write ? writeSessions : readSessions;

        long threadId = Thread.currentThread().getId();
        if (!sessions.containsKey(threadId)) {
            setUpSession(threadId);
        }

        return Collections.singletonMap(
                new Pair<>(threadId, queryType), sessions.get(threadId).run(queryToExecute).consume()
        );
    }

    private void setUpSession(long threadId) {
        Session session = driver.session(
                SessionConfig.builder()
                        .withDatabase(database)
                        .withDefaultAccessMode(write ? AccessMode.WRITE : AccessMode.READ)
                        .build()
        );
        (write ? writeSessions : readSessions).putIfAbsent(threadId, session);
    }

    /**
     * Count all the edges of a random type that are originating in a random vertex
     * @param driver driver
     * @param database database
     * @return Query
     */
    public static Query CountAssocQuery(Driver driver, String database) {
        int maxNode = NodeType.RESOURCE.getMaxValue();
        int randomNum = ThreadLocalRandom.current().nextInt(0, maxNode + 1);
        NodeType nodeType = NodeType.getFromInt(randomNum);
        EdgeType edgeType = EdgeType.getForNodeType(nodeType);

        String queryToExecute = String.format(
                "MATCH (s:%s)-[r:%s]-() WHERE s.%s = '%d' RETURN count(r)",
                nodeType.getNodeType(),
                edgeType.getEdgeType(),
                nodeType.getIdField(),
                randomNum
        );

        return new Query(false, 4, queryToExecute, driver, database);
    }

    /**
     * Fetch a random node by id
     * @param driver driver
     * @param database databasdatabasee
     * @return Query
     */
    public static Query GetNodesQuery(Driver driver, String database) {
        int maxNode = NodeType.RESOURCE.getMaxValue();
        int randomNum = ThreadLocalRandom.current().nextInt(0, maxNode + 1);
        NodeType type = NodeType.getFromInt(randomNum);

        String queryToExecute = String.format("MATCH (s:%s) WHERE s.%s = '%d' RETURN s",
                type.getNodeType(),
                type.getIdField(),
                randomNum
        );

        return new Query(false, 0, queryToExecute, driver, database);
    }

    /**
     * Create a query that inserts an edge between to random vertices.
     * @param driver driver
     * @param database database
     */
    public static Query InsertAssocQuery(Driver driver, String database) {
        int maxNode = NodeType.RESOURCE.getMaxValue();
        int randomNum = ThreadLocalRandom.current().nextInt(0, maxNode + 1);
        NodeType nodeType = NodeType.getFromInt(randomNum);
        int randomNum2 = ThreadLocalRandom.current().nextInt(0, maxNode + 1);
        NodeType nodeType2 = NodeType.getFromInt(randomNum2);
        EdgeType edgeType = EdgeType.getForNodeTypes(nodeType, nodeType2);

        String queryToExecute = String.format(
                "MATCH (s:%s), (p:%s) WHERE s.%s = '%d' AND p.%s = '%d' CREATE (s)-[r:%s]->(p)",
                nodeType.getNodeType(),
                nodeType2.getNodeType(),
                nodeType.getIdField(),
                randomNum,
                nodeType2.getIdField(),
                randomNum2,
                edgeType.getEdgeType()
        );

        return new Query(true, 6, queryToExecute, driver, database);
    }

    /**
     * Create a query that inserts a node of a random type with dummy data
     * @param driver driver
     * @param database database
     * @return Query
     */
    public static Query InsertNodeQuery(Driver driver, String database) {
        String queryToExecute;

        NodeType node = NodeType.getRandomNodeType();
        switch (node) {
            case COMPANY:
                queryToExecute = String.format(
                        "CREATE (n:%s {name: '%s', type: '%s', revenue: '%d'})",
                        node.getNodeType(),
                        RandomStringUtils.randomAlphabetic(100),
                        RandomStringUtils.randomAlphabetic(10),
                        ThreadLocalRandom.current().nextInt(0, 1_000_000_000)
                );
                break;

            case PERSON:
                queryToExecute = String.format(
                        "CREATE (n:%s {firstName: '%s', lastName: '%s', email: '%s'})",
                        node.getNodeType(),
                        RandomStringUtils.randomAlphabetic(100),
                        RandomStringUtils.randomAlphabetic(100),
                        RandomStringUtils.randomAlphabetic(1000)
                );
                break;

            case PLACE:
                queryToExecute = String.format(
                        "CREATE (n:%s {name: '%s', longitude: '%d', latitude: '%d'})",
                        node.getNodeType(),
                        RandomStringUtils.randomAlphabetic(100),
                        ThreadLocalRandom.current().nextInt(0, 12960000),
                        ThreadLocalRandom.current().nextInt(0, 6480000)
                );
                break;

            case PROJECT:
                queryToExecute = String.format(
                        "CREATE (n:%s {name: '%s', budget: '%d'})",
                        node.getNodeType(),
                        RandomStringUtils.randomAlphabetic(100),
                        ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE)
                );
                break;

            case RESOURCE:
                queryToExecute = String.format(
                        "CREATE (n:%s {name: '%s', formula: '%s', density: '%d', meltingPoint: '%d'})",
                        node.getNodeType(),
                        RandomStringUtils.randomAlphabetic(100),
                        RandomStringUtils.randomAlphabetic(100),
                        ThreadLocalRandom.current().nextInt(0, 1000000),
                        ThreadLocalRandom.current().nextInt(0, 100000)
                );
                break;

            default:
                throw new RuntimeException("Node not known!");
        }

        return new Query(true, 1, queryToExecute, driver, database);
    }

    /**
     * Delete a random node
     * @param driver driver
     * @param database database
     * @return Query
     */
    public static Query DeleteNodeQuery(Driver driver, String database) {
        int maxNode = NodeType.RESOURCE.getMaxValue();
        int randomNum = ThreadLocalRandom.current().nextInt(0, maxNode + 1);
        NodeType type = NodeType.getFromInt(randomNum);

        String queryToExecute = String.format("MATCH (s:%s) WHERE s.%s = '%d' DELETE (s)",
                type.getNodeType(),
                type.getIdField(),
                randomNum
        );

        return new Query(false, 2, queryToExecute, driver, database);
    }

    /**
     * Return all relations outgoing from a given vertex
     * @param driver driver
     * @param database database
     * @return Query
     */
    public static Query RangeAssocQuery(Driver driver, String database) {
        int maxNode = NodeType.RESOURCE.getMaxValue();
        int randomNum = ThreadLocalRandom.current().nextInt(0, maxNode + 1);
        NodeType nodeType = NodeType.getFromInt(randomNum);
        EdgeType edgeType = EdgeType.getForNodeType(nodeType);

        String queryToExecute = String.format(
                "MATCH (s:%s)-[r:%s]-() WHERE s.%s = '%d' RETURN r",
                nodeType.getNodeType(),
                edgeType.getEdgeType(),
                nodeType.getIdField(),
                randomNum
        );

        return new Query(false, 5, queryToExecute, driver, database);
    }

    /**
     * Create query that updates the name of a random node.
     * @param driver driver
     * @param database database
     * @return Query
     */
    public static Query UpdateNodeQuery(Driver driver, String database) {
        int maxNode = NodeType.RESOURCE.getMaxValue();
        int randomNum = ThreadLocalRandom.current().nextInt(0, maxNode + 1);
        NodeType type = NodeType.getFromInt(randomNum);

        String queryToExecute;

        if (type == NodeType.PERSON) {
            queryToExecute = String.format(
                    "MATCH (s:%s) WHERE s.%s = '%d' SET s.firstName = '%s', s.lastName = '%s'",
                    type.getNodeType(),
                    type.getIdField(),
                    randomNum,
                    RandomStringUtils.randomAlphabetic(100),
                    RandomStringUtils.randomAlphabetic(100)
            );
        } else {
            queryToExecute = String.format(
                    "MATCH (s:%s) WHERE s.%s = '%d' SET s.name = '%s'",
                    type.getNodeType(),
                    type.getIdField(),
                    randomNum,
                    RandomStringUtils.randomAlphabetic(100)
            );
        }

        return new Query(true, 3, queryToExecute, driver, database);
    }

}
