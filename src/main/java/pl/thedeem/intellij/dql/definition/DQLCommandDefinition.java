package pl.thedeem.intellij.dql.definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DQLCommandDefinition {
    public String name;
    public String description;
    public String type;
    public List<DQLParameterDefinition> parameters;
    public List<DQLParameterDefinition> requiredParameters;
    public Map<String, DQLParameterDefinition> paramsByName;
    public List<String> syntax;
    public List<String> oneOfRequired;
    public DQLCommandGroup commandGroup;
    public Boolean injectMetricParameters;

    public void initialize() {
        paramsByName = new HashMap<>();
        requiredParameters = new ArrayList<>();
        commandGroup = DQLCommandGroup.getGroup(type);
        if (parameters != null) {
            for (DQLParameterDefinition parameter : parameters) {
                paramsByName.put(parameter.name.toLowerCase(), parameter);
                if (parameter.required) {
                    requiredParameters.add(parameter);
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public DQLParameterDefinition getParameter(String name) {
        return name != null ? paramsByName.get(name.toLowerCase()) : null;
    }

    public DQLCommandGroup getCommandGroup() {
        return commandGroup;
    }

    public List<DQLParameterDefinition> getRequiredParameters() {
        return requiredParameters;
    }

    public List<DQLParameterDefinition> getExclusiveRequiredParameters() {
        if (oneOfRequired != null && !oneOfRequired.isEmpty()) {
            return oneOfRequired.stream().map(p -> paramsByName.get(p.toLowerCase())).collect(Collectors.toList());
        }
        return List.of();
    }

    public boolean shouldInjectMetricParameters() {
        return Boolean.TRUE.equals(injectMetricParameters);
    }
}
