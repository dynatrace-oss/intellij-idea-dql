package pl.thedeem.intellij.dql.services.parameters;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.DQLBracketExpression;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.model.MappedParameter;

import java.util.*;

public class DQLParametersCalculatorServiceImpl implements DQLParametersCalculatorService {
    private final Set<String> PSEUDO_PARAMETER_NAMES = Set.of("alias");

    @Override
    public @NotNull List<MappedParameter> mapParameters(@NotNull DQLParametersOwner holder, @NotNull List<Parameter> definitions) {
        List<DQLExpression> toProcess = new ArrayList<>(holder.getParameterExpressions());

        Set<String> usedNamedParams = collectNamedParameterNames(toProcess);
        Map<String, Parameter> definitionsByName = buildDefinitionsByName(definitions);
        List<Parameter> unfilledUnnamed = buildUnfilledUnnamedParameters(definitions, usedNamedParams);
        // DQL requires either 1-element values or values enclosed within brackets if the variadic requires name or there are more unnamed variadic params
        boolean allVariadicRequireBrackets = definitions.stream().filter(p -> p.variadic() && !p.requiresName()).toList().size() > 1;

        List<MappedParameter> result = new ArrayList<>();
        MappedParameter activeVariadic = null;

        while (!toProcess.isEmpty()) {
            DQLExpression currentExpression = toProcess.removeFirst();
            DQLExpression flattened = DQLUtil.flattenBrackets(currentExpression);
            List<DQLExpression> nestedParameters = flattened != null ? findNestedParameters(flattened) : List.of();
            toProcess.addAll(0, nestedParameters);
            if (flattened instanceof DQLParameterExpression named) {
                String name = named.getName() != null ? named.getName().toLowerCase() : "";
                Parameter definition = definitionsByName.get(name);
                boolean pseudo = definition == null && PSEUDO_PARAMETER_NAMES.stream().anyMatch(p -> StringUtil.equalsIgnoreCase(p, name));
                if (!pseudo) {
                    MappedParameter parameter = new MappedParameter(definition, currentExpression);
                    result.add(parameter);
                    if (canBecomeVariadic(activeVariadic, allVariadicRequireBrackets, definition)) {
                        activeVariadic = parameter;
                    }
                } else {
                    if (activeVariadic != null) {
                        if (activeVariadic.holder().getParent() == currentExpression.getParent()) {
                            activeVariadic.addChildExpression(currentExpression);
                        }
                    } else {
                        Parameter nextDefinition = unfilledUnnamed.getFirst();
                        if (nextDefinition != null && nextDefinition.variadic()) {
                            unfilledUnnamed.removeFirst();
                            MappedParameter parameter = new MappedParameter(nextDefinition, currentExpression, false);
                            activeVariadic = parameter;
                            result.add(parameter);
                        } else {
                            result.add(new MappedParameter(null, currentExpression, true));
                        }
                    }
                }
                continue;
            }

            if (activeVariadic != null) {
                activeVariadic.addChildExpression(currentExpression);
                continue;
            }

            if (!unfilledUnnamed.isEmpty()) {
                Parameter definition = unfilledUnnamed.removeFirst();
                MappedParameter parameter = new MappedParameter(definition, currentExpression);
                result.add(parameter);
                if (canBecomeVariadic(null, allVariadicRequireBrackets, definition)) {
                    activeVariadic = parameter;
                }
                continue;
            }

            result.add(new MappedParameter(null, currentExpression));
        }

        return result;
    }

    private @NotNull List<DQLExpression> findNestedParameters(@NotNull DQLExpression flattened) {
        List<DQLExpression> nested = new ArrayList<>();
        if (!(flattened instanceof DQLBracketExpression brackets)) {
            return nested;
        }
        List<DQLExpression> toProcess = new ArrayList<>(brackets.getExpressionList());
        while (!toProcess.isEmpty()) {
            DQLExpression current = toProcess.removeFirst();
            if (current instanceof DQLBracketExpression bracket) {
                toProcess.addAll(0, bracket.getExpressionList());
                continue;
            }
            if (current instanceof DQLParameterExpression) {
                nested.add(current);
            }
        }
        return nested;
    }

    private boolean canBecomeVariadic(
            @Nullable MappedParameter activeVariadic,
            boolean allVariadicRequireBrackets,
            @Nullable Parameter definition
    ) {
        // Only values in brackets are allowed for such cases - and they will be a single elements
        if (allVariadicRequireBrackets) {
            return false;
        }
        if (activeVariadic != null) {
            return false;
        }
        if (definition == null) {
            return false;
        }
        return definition.variadic() && !definition.requiresName();
    }

    private @NotNull Map<String, Parameter> buildDefinitionsByName(@NotNull List<Parameter> definitions) {
        Map<String, Parameter> map = new HashMap<>();
        for (Parameter def : definitions) {
            map.put(def.name().toLowerCase(), def);
        }
        return map;
    }

    private @NotNull List<Parameter> buildUnfilledUnnamedParameters(
            @NotNull List<Parameter> definitions,
            @NotNull Set<String> usedNamedParams
    ) {
        List<Parameter> unfilled = new ArrayList<>();
        for (Parameter def : definitions) {
            if (!def.requiresName() && usedNamedParams.stream().noneMatch(n -> n.equalsIgnoreCase(def.name()))) {
                unfilled.add(def);
            }
        }
        return unfilled;
    }

    private @NotNull Set<String> collectNamedParameterNames(@NotNull List<DQLExpression> expressions) {
        Set<String> names = new HashSet<>();
        List<DQLExpression> toProcess = new ArrayList<>(expressions);
        while (!toProcess.isEmpty()) {
            DQLExpression expression = toProcess.removeFirst();
            if (expression instanceof DQLBracketExpression bracket) {
                toProcess.addAll(0, bracket.getExpressionList());
                continue;
            }
            if (expression instanceof DQLParameterExpression named && named.getName() != null) {
                names.add(named.getName());
            }
        }
        return Set.copyOf(names);
    }
}
