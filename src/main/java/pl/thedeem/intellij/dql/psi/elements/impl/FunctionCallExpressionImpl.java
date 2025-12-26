package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.definition.model.Signature;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionName;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.FunctionCallExpression;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.services.parameters.DQLParametersCalculatorService;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class FunctionCallExpressionImpl extends ASTWrapperPsiElement implements FunctionCallExpression {
    private CachedValue<List<MappedParameter>> parameters;
    private CachedValue<Function> definition;

    public FunctionCallExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        DQLFunctionName functionName = findChildByClass(DQLFunctionName.class);
        return functionName != null ? functionName.getName() : getText();
    }

    @Override
    public @NotNull Collection<String> getDataType() {
        Signature signature = getSignature();
        if (signature == null) {
            return Set.of();
        }
        return signature.outputs();
    }

    @Override
    public @NotNull List<MappedParameter> getParameters() {
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
    public @NotNull Collection<Parameter> getMissingRequiredParameters() {
        Signature signature = getSignature();
        if (signature == null) {
            return List.of();
        }
        return DQLUtil.getMissingParameters(getParameters(), signature.requiredParameters());
    }

    @Override
    public @NotNull Collection<Parameter> getMissingParameters() {
        Signature signature = getSignature();
        if (signature == null) {
            return List.of();
        }
        return DQLUtil.getMissingParameters(getParameters(), signature.parameters());
    }

    @Override
    public MappedParameter getParameter(@NotNull PsiElement argument) {
        return getParameters().stream().filter(m -> m.includes(argument)).findFirst().orElse(null);
    }

    @Override
    public @Nullable Function getDefinition() {
        if (definition == null) {
            definition = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateDefinition(), this),
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
        return new StandardItemPresentation(getName(), this, DQLIcon.DQL_FUNCTION);
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance(getProject()).calculateFieldName(
                getName(),
                "(",
                new DQLFieldNamesService.SeparatedChildren(getFunctionArguments(), ", "),
                ")"
        );
    }

    @Override
    public @Nullable MappedParameter findParameter(@NotNull String name) {
        return getParameters().stream().filter(p -> name.equals(p.name())).findFirst().orElse(null);
    }

    @Override
    public @Nullable Signature getSignature() {
        Function definition = getDefinition();
        if (definition == null) {
            return null;
        }
        // currently, all functions have only one signature so we do not have to worry about finding the most matching one
        return definition.signatures().isEmpty() ? null : definition.signatures().getFirst();
    }

    private List<MappedParameter> recalculateParameters() {
        Signature signature = getSignature();
        if (signature == null) {
            return List.of();
        }
        List<DQLExpression> defined = getFunctionArguments();
        List<Parameter> available = Objects.requireNonNullElse(signature.parameters(), List.of());
        DQLParametersCalculatorService service = DQLParametersCalculatorService.getInstance(getProject());
        return service.mapParameters(defined, available);
    }

    private @Nullable Function recalculateDefinition() {
        DQLDefinitionService service = DQLDefinitionService.getInstance(getProject());
        List<Function> definitions = service.getFunctionByName(Objects.requireNonNullElse(this.getName(), ""));
        if (definitions.isEmpty()) {
            return null;
        }
        if (definitions.size() == 1) {
            return definitions.getFirst();
        }
        List<PsiElement> parents = PsiUtils.getElementsUntilParent(this, DQLCommand.class);
        if (parents.getFirst() instanceof DQLCommand statement && parents.get(1) instanceof DQLExpression expression) {
            MappedParameter parameter = statement.getParameter(expression);
            Parameter parameterDefinition = parameter != null ? parameter.definition() : null;
            if (parameterDefinition != null) {
                Collection<String> matchingCategories = service.getFunctionCategoriesForParameterTypes(parameterDefinition.parameterValueTypes());
                return matchingCategories != null ? definitions.stream().filter(d -> matchingCategories.contains(d.category())).findFirst().orElse(null) : null;
            }
        }
        return null;
    }
}
