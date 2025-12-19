package pl.thedeem.intellij.dql.definition;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        boolean conflictingVariadic = definitions.stream().filter(Parameter::variadic).count() > 1;
        for (DQLExpression defined : definedParameters) {
            MappedParameter mappedParameter = null;
            if (defined instanceof DQLParameterExpression named) {
                mappedParameter = createNamedParameter(named, availableParameters);
            } else if (variadic == null) {
                mappedParameter = createUnnamedParameter(defined, availableParameters, unusedParameters);
            } else {
                variadic.included().add(defined);
            }
            if (mappedParameter != null) {
                result.add(mappedParameter);
                variadic = calculateVariadic(mappedParameter, variadic, conflictingVariadic);
            }
        }
        return result;
    }

    private @Nullable MappedParameter calculateVariadic(@NotNull MappedParameter parameter, @Nullable MappedParameter currentVariadic, boolean conflictingVariadic) {
        Parameter definition = parameter.definition();
        if (definition == null) {
            return currentVariadic;
        }
        // sticky variadic only work for parameters that do not require name
        // If the parameter value is already enclosed with `{}`, it does not stick
        if (definition.variadic() && !definition.requiresName()) {
            PsiElement toCheck = parameter.holder() instanceof DQLParameterExpression named ? named.getExpression() : parameter.holder();
            return conflictingVariadic && toCheck instanceof DQLBracketExpression ? currentVariadic : parameter;
        }
        return currentVariadic;
    }

    private @NotNull MappedParameter createNamedParameter(@NotNull DQLParameterExpression named, @NotNull Map<String, Parameter> availableParameters) {
        String name = Objects.requireNonNull(named.getName());
        Parameter parameter = availableParameters.get(name.toLowerCase());
        return new MappedParameter(
                parameter,
                named,
                new ArrayList<>()
        );
    }

    private @NotNull MappedParameter createUnnamedParameter(@NotNull DQLExpression expression, @NotNull Map<String, Parameter> availableParameters, @NotNull List<String> unusedParameters) {
        if (unusedParameters.isEmpty()) {
            return new MappedParameter(null, expression, new ArrayList<>());
        }
        String name = unusedParameters.removeFirst();
        Parameter parameter = availableParameters.get(name.toLowerCase());
        return new MappedParameter(
                parameter,
                expression,
                new ArrayList<>()
        );
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
