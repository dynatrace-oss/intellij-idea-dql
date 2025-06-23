package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.*;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionName;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.FunctionCallExpression;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.*;
import java.util.stream.Collectors;

public abstract class FunctionCallExpressionImpl extends ASTWrapperPsiElement implements FunctionCallExpression {
    private CachedValue<List<DQLParameterObject>> parameters;
    private CachedValue<DQLFunctionDefinition> definition;
    private CachedValue<Map<DQLExpression, DQLParameterObject>> parameterMapping;

    public FunctionCallExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        DQLFunctionName functionName = findChildByClass(DQLFunctionName.class);
        return functionName != null ? functionName.getName() : getText();
    }

    @Override
    public Set<DQLDataType> getDataType() {
        DQLFunctionDefinition functionDefinition = getDefinition();
        // for unknown functions, we do not to have errors
        if (functionDefinition == null) {
            return Set.of(DQLDataType.ANY);
        }

        Set<DQLDataType> result = new HashSet<>(functionDefinition.getDQLTypes());
        DQLDataType groupType = DQLDataType.getType(functionDefinition.getFunctionGroup());
        if (groupType != null) {
            result.add(groupType);
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public @NotNull List<DQLParameterObject> getParameters() {
        if (parameters == null) {
            parameters = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateParameters(), this),
                    false
            );
        }
        return parameters.getValue();
    }

    @Override
    public @NotNull List<DQLExpression> getFunctionArguments() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DQLExpression.class);
    }

    @Override
    public @NotNull Set<DQLParameterDefinition> getDefinedParameters() {
        return getParameters().stream()
                .map(DQLParameterObject::getDefinition)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public @NotNull Set<String> getDefinedParameterNames() {
        return getParameters().stream()
                .filter(p -> p.getDefinition() != null)
                .map(p -> p.getDefinition().name)
                .collect(Collectors.toSet());
    }

    @Override
    public @NotNull List<DQLParameterDefinition> getMissingRequiredParameters() {
        DQLFunctionDefinition definition = getDefinition();
        if (definition == null) {
            return List.of();
        }

        Set<DQLParameterDefinition> definedDefinitions = getDefinedParameters();
        return definition.getRequiredParameters().stream().filter(p -> !definedDefinitions.contains(p)).toList();
    }

    @Override
    public DQLParameterObject getParameter(@NotNull DQLExpression argument) {
        if (parameterMapping == null) {
            parameterMapping = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateParameterMapping(), this, parameters),
                    false
            );
        }
        return parameterMapping.getValue().get(argument);
    }

    @Override
    public DQLFunctionDefinition getDefinition() {
        if (definition == null) {
            definition = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(DQLFunctionsLoader.getFunction(this.getName()), this),
                    false
            );
        }
        return definition.getValue();
    }

    @Override
    public boolean accessesData() {
        List<DQLExpression> defined = getFunctionArguments();
        for (DQLExpression expression : defined) {
            if (expression instanceof BaseTypedElement entity && entity.accessesData()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(getName(), this, DQLIcon.DQL_FUNCTION);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator()
                .addPart(getName())
                .addPart("(")
                .addPart(getFunctionArguments(), ",")
                .addPart(")")
                .getFieldName();
    }

    private List<DQLParameterObject> recalculateParameters() {
        DQLFunctionDefinition definition = getDefinition();
        List<DQLExpression> defined = getFunctionArguments();
        DQLParametersMapper mapper = new DQLParametersMapper(
                definition != null ? definition.getParameters((DQLFunctionCallExpression) this) : List.of()
        );
        return mapper.map(defined);
    }

    private Map<DQLExpression, DQLParameterObject> recalculateParameterMapping() {
        Map<DQLExpression, DQLParameterObject> parameterMapping = new HashMap<>();
        for (DQLParameterObject parameter : getParameters()) {
            parameterMapping.put(parameter.getExpression(), parameter);
        }
        return Collections.unmodifiableMap(parameterMapping);
    }
}
