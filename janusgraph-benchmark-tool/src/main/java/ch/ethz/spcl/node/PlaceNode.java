package ch.ethz.spcl.node;

import ch.ethz.spcl.query.QueryResultSummary;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class PlaceNode extends Node {
    public static final int MIN_VALUE = 1677721;
    public static final int MAX_VALUE = 2097151;

    public PlaceNode() {
        super(NodeType.Place.toString().toLowerCase(Locale.ROOT) + "Id");
        nodeType = NodeType.Place;
    }

    public static long getMinValue() {
        return PlaceNode.MIN_VALUE;
    }

    public static long getMaxValue() {
        return PlaceNode.MAX_VALUE;
    }

    public Map<String, Object> getRandomProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", RandomStringUtils.randomAlphabetic(100));
        properties.put("longitude", ThreadLocalRandom.current().nextInt(0, 12960000));
        properties.put("latitude", ThreadLocalRandom.current().nextInt(0, 6480000));

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
