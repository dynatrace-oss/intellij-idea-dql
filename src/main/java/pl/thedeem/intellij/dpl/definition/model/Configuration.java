package pl.thedeem.intellij.dpl.definition.model;

import java.util.Objects;

public record Configuration(
        String name,
        Object defaultValue,
        String description,
        String type
) {
    public String suggestion() {
        String result = " = ";
        result += switch (type) {
            case "float", "integer", "long" -> Objects.requireNonNullElse(defaultValue, "0");
            case null, default -> "\"" + Objects.requireNonNullElse(defaultValue, "") + "\"";
        };
        return result;
    }
}
