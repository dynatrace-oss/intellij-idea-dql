package pl.thedeem.intellij.dpl.definition.model;

public record CommandMatcher(
        String type,
        String key,
        String value,
        boolean required
) {
}
