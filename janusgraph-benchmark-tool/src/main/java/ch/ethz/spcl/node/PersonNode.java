package ch.ethz.spcl.node;

import ch.ethz.spcl.query.QueryResultSummary;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PersonNode extends Node {
    public static final int MIN_VALUE = 419430;
    public static final int MAX_VALUE = 1677720;

    public PersonNode() {
        super(NodeType.Person.toString().toLowerCase(Locale.ROOT) + "Id");
        nodeType = NodeType.Person;
    }

    public static long getMinValue() {
        return PersonNode.MIN_VALUE;
    }

    public static long getMaxValue() {
        return PersonNode.MAX_VALUE;
    }

    public Map<String, Object> getRandomProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("firstName", RandomStringUtils.randomAlphanumeric(100));
        properties.put("lastName", RandomStringUtils.randomAlphanumeric(100));
        properties.put("email", RandomStringUtils.randomAlphanumeric(1000));

        return properties;
    }

    @Override
    public Map<Long, QueryResultSummary> updateProperties(
            JanusGraph graph,
            GraphTraversalSource source,
            long vertexId
    ) {
        long id = Thread.currentThread().getId();
        String lastName = RandomStringUtils.randomAlphanumeric(100);
        try {
            long startTime = System.nanoTime();
            source.V().has("id", vertexId).property("lastName", lastName).next();
            graph.tx().commit();
            long endTime = System.nanoTime();

            // obj_update has id 3
            return Collections.singletonMap(id, new QueryResultSummary(3, endTime - startTime, 0));
        } catch (Exception e) {
            return Collections.singletonMap(id, new QueryResultSummary(3, -1, 1));
        }
    }
}
