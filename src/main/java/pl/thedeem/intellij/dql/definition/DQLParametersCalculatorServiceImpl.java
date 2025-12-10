package pl.thedeem.intellij.dql.definition;

import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLBracketExpression;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;

import java.util.*;
import java.util.stream.Collectors;

public class DQLParametersCalculatorServiceImpl implements DQLParametersCalculatorService {
    @Override
    public @NotNull List<MappedParameter> mapParameters(@NotNull List<DQLExpression> definedParameters, @NotNull List<Parameter> definitions) {
        Set<String> usedParameters = usedNamedParameters(definedParameters);
        List<String> unusedParameters = new ArrayList<>(unusedParameters(usedParameters, definitions));
        Map<String, Parameter> availableParameters = availableParameters(definitions);
        List<MappedParameter> result = new ArrayList<>();

        MappedParameter variadic = null;
        for (DQLExpression defined : definedParameters) {
            if (defined instanceof DQLParameterExpression named) {
                String name = Objects.requireNonNull(named.getName());
                Parameter parameter = availableParameters.get(name.toLowerCase());
                MappedParameter mappedParameter = new MappedParameter(
                        parameter,
                        defined,
                        new ArrayList<>()
                );
                result.add(mappedParameter);
                variadic = parameter == null || !parameter.variadic() || named.getExpression() instanceof DQLBracketExpression ? null : mappedParameter;
            } else {
                if (variadic == null) {
                    if (unusedParameters.isEmpty()) {
                        result.add(new MappedParameter(null, defined, new ArrayList<>()));
                    } else {
                        String name = unusedParameters.removeFirst();
                        Parameter parameter = availableParameters.get(name.toLowerCase());
                        MappedParameter mappedParameter = new MappedParameter(
                                parameter,
                                defined,
                                new ArrayList<>()
                        );
                        result.add(mappedParameter);
                        variadic = parameter == null || !parameter.variadic() || defined instanceof DQLBracketExpression ? null : mappedParameter;
                    }
                } else {
                    variadic.included().add(defined);
                }
            }
        }

        return result;
    }

    private @NotNull Map<String, Parameter> availableParameters(@NotNull List<Parameter> definitions) {
        Map<String, Parameter> result = new HashMap<>();
        for (Parameter definition : definitions) {
            result.put(definition.name().toLowerCase(), definition);
            if (definition.aliases() != null) {
                for (String alias : definition.aliases()) {
                    result.put(alias.toLowerCase(), definition);
                }
            }
        }

        return result;
    }

    private @NotNull Set<String> usedNamedParameters(@NotNull List<DQLExpression> definedParameters) {
        return definedParameters.stream()
                .filter(d -> d instanceof DQLParameterExpression expr && expr.getName() != null)
                .map(d -> ((DQLParameterExpression) d).getName())
                .collect(Collectors.toSet());
    }

    private @NotNull List<String> unusedParameters(@NotNull Set<String> usedParameters, @NotNull List<Parameter> definitions) {
        return definitions.stream()
                .filter(p -> !p.hidden())
                .sorted(Comparator.comparing(Parameter::requiresName))
                .map(Parameter::name)
                .filter(name -> !usedParameters.contains(name))
                .toList();
    }
}
