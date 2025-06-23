package pl.thedeem.intellij.dql.definition;

import java.util.Arrays;
import java.util.List;

public enum DQLCommandGroup {
    AGGREGATION("aggregation-commands"),
    CORRELATION_AND_JOIN("correlation-and-join-commands"),
    DATA_SOURCE("data-source-commands"),
    EXTRACTION_AND_PARSING("extraction-and-parsing-commands"),
    FILTERING("filtering-commands"),
    METRIC("metric-commands"),
    ORDERING("ordering-commands"),
    SELECTION_AND_MODIFICATION("selection-and-modification-commands"),
    STRUCTURING("structuring-commands"),
    ;

    public static final List<DQLCommandGroup> STARTING_COMMAND_TYPES = List.of(DQLCommandGroup.DATA_SOURCE, DQLCommandGroup.METRIC);
    public static final List<DQLCommandGroup> EXTENSION_COMMAND_TYPES = List.of(
            DQLCommandGroup.AGGREGATION, DQLCommandGroup.CORRELATION_AND_JOIN, DQLCommandGroup.EXTRACTION_AND_PARSING,
            DQLCommandGroup.FILTERING, DQLCommandGroup.ORDERING, DQLCommandGroup.SELECTION_AND_MODIFICATION, DQLCommandGroup.STRUCTURING
    );
    private final String name;

    DQLCommandGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static DQLCommandGroup getGroup(String groupName) {
        return Arrays.stream(values()).filter(group -> group.getName().equals(groupName)).findFirst().orElse(null);
    }
}
