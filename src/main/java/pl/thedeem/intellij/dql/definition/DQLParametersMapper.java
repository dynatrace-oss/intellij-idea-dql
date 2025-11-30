package pl.thedeem.intellij.dql.definition;

import pl.thedeem.intellij.dql.psi.DQLBracketExpression;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.*;
import java.util.stream.Collectors;

public class DQLParametersMapper {
    private final List<DQLParameterDefinition> availableParameters;
    private final Map<String, DQLParameterDefinition> paramsByName;

    public DQLParametersMapper(List<DQLParameterDefinition> availableParameters) {
        this.availableParameters = availableParameters;

        this.paramsByName = new HashMap<>();
        availableParameters.forEach(p -> paramsByName.put(p.name.toLowerCase(), p));
    }

    public List<DQLParameterObject> map(List<DQLExpression> definedParameters) {
        Set<String> usedNamedParameters = getNamedParameters(definedParameters);
        List<String> missingParameters = getMissingParameters(usedNamedParameters);
        List<DQLParameterObject> result = new ArrayList<>();
        Map<String, List<DQLExpression>> mapping = new HashMap<>();

        List<DQLExpression> parameters = new ArrayList<>(definedParameters);
        DQLParameterDefinition repetitive = null;
        while (!parameters.isEmpty()) {
            DQLExpression parameter = parameters.removeFirst();

            String parameterName = null;

            // param1: value, value2, {} - the "{}" is probably another parameter
            if (parameter instanceof DQLBracketExpression && repetitive != null && !DQLDataType.LIST_OF_EXPRESSIONS.satisfies(repetitive.getDQLTypes())) {
                missingParameters.remove(repetitive.name);
                repetitive = null;
            }
            if (parameter instanceof DQLParameterExpression parameterExpression) {
                parameterName = parameterExpression.getName();
                if (parameterName != null && repetitive != null && !parameterName.equals(repetitive.name)) {
                    missingParameters.remove(repetitive.name);
                    repetitive = null;
                }
            } else if (repetitive != null) {
                parameterName = repetitive.name;
            } else if (!missingParameters.isEmpty()) {
                parameterName = missingParameters.getFirst();
            }

            if (parameterName != null) {
                DQLParameterDefinition definition = this.paramsByName.get(parameterName.toLowerCase());
                if (definition == null) {
                    missingParameters.remove(parameterName);
                } else if (definition.repetitive && !definition.singleUnnamed) {
                    repetitive = definition;
                } else {
                    missingParameters.remove(parameterName);
                    if (definition.singleUnnamed) {
                        repetitive = null;
                    }
                }

                mapping.putIfAbsent(parameterName, new ArrayList<>());
                List<DQLExpression> related = mapping.get(parameterName);
                related.add(parameter);
                result.add(new DQLParameterObject(definition, parameter, related));

                // clear repetitive if the value is bracketed
                if (parameter instanceof DQLBracketExpression || (parameter instanceof DQLParameterExpression named && named.getExpression() instanceof DQLBracketExpression)) {
                    repetitive = null;
                }
            } else {
                result.add(new DQLParameterObject(null, parameter, List.of()));
            }
        }


        return result;
    }

    private Set<String> getNamedParameters(List<DQLExpression> defined) {
        return defined.stream()
                .filter(d -> d instanceof DQLParameterExpression expr && expr.getName() != null)
                .map(d -> ((DQLParameterExpression) d).getName())
                .collect(Collectors.toSet());
    }

    // List because the order is important
    private List<String> getMissingParameters(Set<String> usedNamedParameters) {
        return new ArrayList<>(this.availableParameters.stream()
                .filter(p -> !usedNamedParameters.contains(p.name))
                .map(p -> p.name)
                .toList());
    }
}
