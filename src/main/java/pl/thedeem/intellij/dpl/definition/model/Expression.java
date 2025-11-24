package pl.thedeem.intellij.dpl.definition.model;

import java.util.Map;

public record Expression(
        Map<String, Configuration> configuration
) {
}
