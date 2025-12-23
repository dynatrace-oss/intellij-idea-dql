package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Operator;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.definition.model.Signature;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;
import pl.thedeem.intellij.dql.psi.elements.OperatorElement;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractOperatorElementImpl extends ASTWrapperPsiElement implements OperatorElement, BaseTypedElement, DQLParametersOwner {
    private CachedValue<Operator> definition;
    private CachedValue<Signature> signature;
    private CachedValue<Collection<String>> dataTypes;
    private CachedValue<List<MappedParameter>> parameters;

    public AbstractOperatorElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean accessesData() {
        for (PsiElement child : getChildren()) {
            if (child instanceof BaseTypedElement entity && entity.accessesData()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull Collection<String> getDataType() {
        if (dataTypes == null) {
            dataTypes = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateDataTypes(), this),
                    false
            );
        }
        return dataTypes.getValue();
    }

    @Override
    public @Nullable Operator getDefinition() {
        if (definition == null) {
            definition = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateDefinition(), this),
                    false
            );
        }
        return definition.getValue();
    }

    @Override
    public @Nullable Signature getSignature() {
        if (signature == null) {
            signature = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateSignature(), this),
                    false
            );
        }
        return signature.getValue();
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
    public @Nullable MappedParameter findParameter(@NotNull String name) {
        return getParameters().stream().filter(p -> name.equals(p.name())).findFirst().orElse(null);
    }

    @Override
    public @NotNull Collection<Parameter> getMissingRequiredParameters() {
        // because operators are part of the language definition, no missing params will ever occur
        return List.of();
    }

    @Override
    public @NotNull Collection<Parameter> getMissingParameters() {
        // because operators are part of the language definition, no missing params will ever occur
        return List.of();
    }

    @Override
    public @Nullable MappedParameter getParameter(@NotNull PsiElement parameter) {
        return getParameters().stream().filter(m -> m.includes(parameter)).findFirst().orElse(null);
    }

    @Override
    public @Unmodifiable @NotNull List<PsiElement> getExpressions() {
        List<PsiElement> expressions = new ArrayList<>();
        PsiElement left = getLeftExpression();
        PsiElement right = getRightExpression();
        if (left != null) {
            expressions.add(left);
        }
        if (right != null) {
            expressions.add(right);
        }
        return expressions;
    }

    @Override
    public @Nullable ExpressionOperatorImpl getOperator() {
        return PsiTreeUtil.getChildOfAnyType(this, ExpressionOperatorImpl.class);
    }

    @Override
    public @Nullable PsiElement getLeftExpression() {
        List<PsiElement> expressions = PsiTreeUtil.getChildrenOfTypeAsList(this, DQLExpression.class);
        return expressions.isEmpty() ? null : expressions.getFirst();
    }

    @Override
    public @Nullable PsiElement getRightExpression() {
        List<PsiElement> expressions = PsiTreeUtil.getChildrenOfTypeAsList(this, DQLExpression.class);
        return expressions.size() > 1 ? expressions.getLast() : null;
    }

    private @NotNull List<MappedParameter> recalculateParameters() {
        Signature signature = getSignature();
        if (signature == null) {
            return List.of();
        }
        int i = 0;
        List<MappedParameter> parameters = new ArrayList<>();
        for (PsiElement child : getExpressions()) {
            Parameter parameter = signature.parameters().size() > i ? signature.parameters().get(i) : null;
            parameters.add(new MappedParameter(parameter, child, List.of()));
            i++;
        }
        return parameters;
    }

    private @Nullable Operator recalculateDefinition() {
        DQLDefinitionService service = DQLDefinitionService.getInstance(getProject());
        String id = getOperationId();
        return service.getOperator(id);
    }

    private @Nullable Signature recalculateSignature() {
        Operator definition = getDefinition();
        if (definition != null && definition.signatures() != null && !definition.signatures().isEmpty()) {
            Map<Integer, Signature> signatures = definition.signatures().stream().collect(Collectors.toMap(e -> e.parameters().size(), e -> e));
            Integer size = getExpressions().size();
            return signatures.getOrDefault(size, definition.signatures().getFirst());
        }
        return null;
    }

    private @NotNull Collection<String> recalculateDataTypes() {
        Operator definition = getDefinition();
        Signature signature = getSignature();
        if (definition == null || signature == null) {
            return Set.of();
        }
        Map<String, Map<String, String>> mapping = definition.resultMapping();
        if (mapping != null && !mapping.isEmpty()) {
            return recalculateDataTypesFromOperands(mapping, signature);
        }
        return signature.outputs();
    }

    private @NotNull Collection<String> recalculateDataTypesFromOperands(@NotNull Map<String, Map<String, String>> mapping, @NotNull Signature signature) {
        if (getLeftExpression() instanceof BaseElement leftElement && getRightExpression() instanceof BaseElement rightElement) {
            Collection<String> leftTypes = leftElement.getDataType();
            Collection<String> rightTypes = rightElement.getDataType();
            if (leftTypes.size() == 1 && rightTypes.size() == 1) {
                String mappedResult = mapping
                        .getOrDefault(leftTypes.stream().toList().getFirst(), Map.of())
                        .get(rightTypes.stream().toList().getFirst());
                if (mappedResult != null) {
                    return Set.of(mappedResult);
                }
            }
            return signature.outputs();
        }

        return signature.outputs();
    }

    protected abstract String getOperationId();

    @Override
    public @Nullable String getOperatorSymbol() {
        ExpressionOperatorImpl operator = PsiTreeUtil.getChildOfType(this, ExpressionOperatorImpl.class);
        if (operator == null) {
            return null;
        }
        return operator.getText().trim().toLowerCase();
    }
}
