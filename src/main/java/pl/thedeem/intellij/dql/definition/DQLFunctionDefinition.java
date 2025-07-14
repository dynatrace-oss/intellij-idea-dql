package pl.thedeem.intellij.dql.definition;

import com.intellij.psi.util.PsiTreeUtil;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

public class DQLFunctionDefinition {
    public String description;
    public String longDescription;
    public String name;
    public String group;
    public List<DQLParameterDefinition> parameters;
    public List<String> syntax;
    public List<String> aliases;
    public List<String> returns;
    public List<String> examples;

    public List<DQLParameterDefinition> requiredParameters = List.of();
    public Map<String, DQLParameterDefinition> nameMapping = Map.of();

    private Set<DQLDataType> dqlTypes;
    private DQLFunctionGroup functionGroup;

    public DQLFunctionDefinition clone(String newName) {
        DQLFunctionDefinition result = new DQLFunctionDefinition();
        result.description = description;
        result.longDescription = longDescription;
        result.name = newName;
        result.group = group;
        result.functionGroup = functionGroup;
        result.parameters = parameters;
        result.syntax = syntax;
        result.aliases = aliases;
        result.returns = returns;
        result.examples = examples;
        result.dqlTypes = dqlTypes;
        result.initialize();
        return result;
    }

    public Set<DQLDataType> getDQLTypes() {
        return dqlTypes;
    }

    @PostConstruct
    public void initialize() {
        dqlTypes = this.returns != null ? this.returns.stream().map(DQLDataType::getType).collect(Collectors.toSet()) : Set.of();
        this.functionGroup = DQLFunctionGroup.getGroup(this.group);
        if (parameters != null) {
            nameMapping = new HashMap<>();
            requiredParameters = new ArrayList<>();
            for (DQLParameterDefinition parameter : parameters) {
                nameMapping.put(parameter.name.toLowerCase(), parameter);
                if (parameter.required) {
                    requiredParameters.add(parameter);
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public List<DQLParameterDefinition> getParameters(DQLFunctionCallExpression context) {
        List<DQLParameterDefinition> result = new ArrayList<>(parameters);
        result.addAll(getTimeSeriesParameters(context));
        return List.copyOf(result);
    }

    public List<DQLParameterDefinition> getRequiredParameters() {
        return requiredParameters;
    }

    public DQLFunctionGroup getFunctionGroup() {
        return functionGroup;
    }

    public List<DQLParameterDefinition> getTimeSeriesParameters(DQLFunctionCallExpression functionCall) {
        DQLQueryStatement statement = PsiTreeUtil.getParentOfType(functionCall, DQLQueryStatement.class);
        if (statement != null && statement.getDefinition() != null) {
            if (statement.getDefinition().shouldInjectMetricParameters()) {
                return DQLDefinitionService.getInstance(functionCall.getProject()).getTimeSeriesParameters();
            }
        }
        return List.of();
    }
}
