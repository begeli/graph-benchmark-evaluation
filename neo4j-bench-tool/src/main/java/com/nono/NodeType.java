package com.nono;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public enum NodeType {

    COMPANY("Company", 0, 419429),
    PERSON("Person", 419430, 1677720),
    PLACE("Place", 1677721, 2097151),
    PROJECT("Project", 2097152, 6291455),
    RESOURCE("Resource", 6291456, 8388607);

    private String nodeType;
    private String idField;
    private int minValue;
    private int maxValue;

    public String getNodeType() {
        return nodeType;
    }

    public String getIdField() {
        return idField;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    NodeType(String nodeType, int minValue, int maxValue) {
        this.nodeType = nodeType;
        this.idField = nodeType.toLowerCase(Locale.ROOT) + "Id";
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Get node by ID. This can be used to get a node type at random,
     * proportional to the number of vertices of each type.
     *
     * @param id to fetch
     * @return node type of the node with given ID
     */
    public static NodeType getFromInt(int id) {
        if (id >= COMPANY.minValue && id <= COMPANY.maxValue) {
            return COMPANY;
        } else if (id >= PERSON.minValue && id <= PERSON.maxValue) {
            return PERSON;
        } else if (id >= PLACE.minValue && id <= PLACE.maxValue) {
            return PLACE;
        } else if (id >= PROJECT.minValue && id <= PROJECT.maxValue) {
            return PROJECT;
        } else if (id >= RESOURCE.minValue && id <= RESOURCE.maxValue) {
            return RESOURCE;
        }
        throw new RuntimeException("Node out of range!");
    }

    /**
     * Select a random node type uniformly at random.
     *
     * @return random node type
     */
    public static NodeType getRandomNodeType() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 5);
        switch (randomNum) {
            case 0: return COMPANY;
            case 1: return PERSON;
            case 2: return PLACE;
            case 3: return PROJECT;
            case 4: return RESOURCE;

            default:
                throw new RuntimeException(String.format("Not possible to generate range for %d", randomNum));
        }
    }
}
