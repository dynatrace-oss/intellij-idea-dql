package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.Operator;
import pl.thedeem.intellij.dql.definition.model.Signature;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.OperatorElement;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class AbstractOperatorElementImpl extends TwoSidesExpressionImpl implements OperatorElement, BaseTypedElement {
    private CachedValue<Operator> definition;
    private CachedValue<Signature> signature;
    private CachedValue<Collection<String>> dataTypes;

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

    private @Nullable Operator recalculateDefinition() {
        DQLDefinitionService service = DQLDefinitionService.getInstance(getProject());
        return service.getOperator(getOperationId());
    }

    private @Nullable Signature recalculateSignature() {
        Operator definition = getDefinition();
        if (definition != null && definition.signatures() != null && !definition.signatures().isEmpty()) {
            return definition.signatures().getFirst();
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
        if (mapping != null) {
            return recalculateDataTypesFromOperands(mapping, signature);
        }
        return signature.outputs();
    }

    private @NotNull Collection<String> recalculateDataTypesFromOperands(@NotNull Map<String, Map<String, String>> mapping, @NotNull Signature signature) {
        DQLExpression leftExpression = getLeftExpression();
        DQLExpression rightExpression = getRightExpression();
        if (leftExpression instanceof BaseElement leftElement && rightExpression instanceof BaseElement rightElement) {
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

    protected String getOperationId() {
        ExpressionOperatorImpl operator = PsiTreeUtil.getChildOfType(this, ExpressionOperatorImpl.class);
        if (operator == null) {
            return null;
        }
        return switch (operator.getText().trim().toLowerCase()) {
            case "-" -> "dql.operator.subtract";
            case "+" -> "dql.operator.add";
            case "!=" -> "dql.operator.notEquals";
            case "==" -> "dql.operator.equals";
            case "and" -> "dql.operator.and";
            case "not" -> "dql.operator.not";
            case "or" -> "dql.operator.or";
            case "xor" -> "dql.operator.xor";
            case "/" -> "dql.operator.divide";
            case ">" -> "dql.operator.greater";
            case ">=" -> "dql.operator.greaterEquals";
            case "<" -> "dql.operator.lower";
            case "<=" -> "dql.operator.lowerEquals";
            case "%" -> "dql.operator.modulo";
            case "*" -> "dql.operator.multiply";
            case "~" -> "dql.operator.search";
            case "@" -> "dql.operator.timeAlignmentAt";
            default -> "";
        };
    }
}
