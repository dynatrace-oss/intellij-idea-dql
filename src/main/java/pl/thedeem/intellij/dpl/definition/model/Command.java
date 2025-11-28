package pl.thedeem.intellij.dpl.definition.model;

import java.util.List;
import java.util.Map;

public record Command(
        String name,
        String description,
        String output,
        List<String> aliases,
        Quantifier quantifier,
        CommandMatcher matchers,
        Map<String, Configuration> configuration
) {
}
