package pl.thedeem.intellij.dql.definition;

import java.util.Arrays;

public enum DQLFunctionGroup {
    AGGREGATE("aggregation-functions"),
    ARRAY("array-functions"),
    BITWISE("bitwise-functions"),
    BOOLEAN("boolean-functions"),
    CONDITIONAL("conditional-functions"),
    CONVERSION_AND_CASTING("conversion-and-casting-functions"),
    GENERAL("general-functions"),
    JOIN("join-functions"),
    MATHEMATICAL("mathematical-functions"),
    NETWORK("network-functions"),
    STRING("string-functions"),
    TIME("time-functions"),
    VECTOR_DISTANCE("vector-distance-functions"),
    ITERATIVE_EXPRESSIONS("iterative-expressions"),
    RECORDS_LIST("records-list-functions", true);

    private final String name;
    private final boolean phony;

    DQLFunctionGroup(String groupName) {
        this.name = groupName;
        this.phony = false;
    }

    DQLFunctionGroup(String groupName, boolean phony) {
        this.name = groupName;
        this.phony = phony;
    }

    public String getName() {
        return name;
    }

    public boolean isPhony() {
        return phony;
    }

    public static DQLFunctionGroup getGroup(String groupName) {
        return Arrays.stream(values()).filter(group -> group.getName().equals(groupName)).findFirst().orElse(null);
    }
}
