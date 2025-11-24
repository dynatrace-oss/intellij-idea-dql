package pl.thedeem.intellij.dpl.definition.model;

import java.util.Map;

public record DPLDefinition(
        Map<String, String> posix,
        Map<String, Command> commands,
        Map<String, Expression> expressions
) {
    public static DPLDefinition empty() {
        return new DPLDefinition(Map.of(), Map.of(), Map.of());
    }
}
