package pl.thedeem.intellij.dpl.definition.model;

public record Quantifier(
        boolean required,
        Long min,
        Long max,
        Long defaultMin,
        Long defaultMax
) {
}
