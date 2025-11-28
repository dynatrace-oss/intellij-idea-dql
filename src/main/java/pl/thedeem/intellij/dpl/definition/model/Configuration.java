package pl.thedeem.intellij.dpl.definition.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record Configuration(
        String name,
        Object defaultValue,
        String description,
        String type,
        Set<String> aliases,
        Set<String> excludes
) {
    public String suggestion() {
        String result = " = ";
        result += switch (type) {
            case "float", "integer", "long" -> Objects.requireNonNullElse(defaultValue, "0");
            case null, default -> "\"" + Objects.requireNonNullElse(defaultValue, "") + "\"";
        };
        return result;
    }

    public Set<String> names() {
        Set<String> result = new HashSet<>();
        result.add(name.toLowerCase());
        if (aliases != null) {
            result.addAll(aliases.stream().map(String::toLowerCase).toList());
        }
        return result;
    }
}
