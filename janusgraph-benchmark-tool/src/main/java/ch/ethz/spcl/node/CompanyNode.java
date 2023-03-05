package ch.ethz.spcl.node;

import ch.ethz.spcl.query.QueryResultSummary;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CompanyNode extends Node {
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 419429;//419429;

    public CompanyNode() {
        super(NodeType.Company.toString().toLowerCase(Locale.ROOT) + "Id");
        nodeType = NodeType.Company;
    }

    public static long getMinValue() {
        return CompanyNode.MIN_VALUE;
    }

    public static long getMaxValue() {
        return CompanyNode.MAX_VALUE;
    }

    public Map<String, Object> getRandomProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", RandomStringUtils.randomAlphanumeric(100));
        properties.put("type", RandomStringUtils.randomAlphanumeric(10));
        properties.put("revenue", ThreadLocalRandom.current().nextInt(0, 1000000000));

        return properties;
    }

    @Override
    public Map<Long, QueryResultSummary> updateProperties(
            JanusGraph graph,
            GraphTraversalSource source,
            long vertexId
    ) {
        long id = Thread.currentThread().getId();
        String name = RandomStringUtils.randomAlphanumeric(100);
        try {
            long startTime = System.nanoTime();
            source.V().has("id", vertexId).property("name", name).next();
            graph.tx().commit();
            long endTime = System.nanoTime();

            // obj_update has id 3
            return Collections.singletonMap(id, new QueryResultSummary(3, endTime - startTime, 0));
        } catch (Exception e) {
            return Collections.singletonMap(id, new QueryResultSummary(3, -1, 1));
        }
    }
}
