package pl.thedeem.intellij.dql.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLOperationTarget;
import pl.thedeem.intellij.dql.definition.DQLOperationsLoader;
import pl.thedeem.intellij.dql.inspections.parameters.parameterValidators.ParameterValidator;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.TwoSidesExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseInspection extends LocalInspectionTool {
    protected boolean isTargetInvalid(DQLExpression expression, Set<Class<?>> allowedExpressions) {
        PsiElement unpacked = DQLUtil.unpackParenthesis(expression);
        return allowedExpressions.stream().anyMatch(t -> t.isInstance(unpacked));
    }

    protected boolean returnValueDoesNotMatch(BaseTypedElement element, Set<DQLDataType> allowed) {
        Set<DQLDataType> returned = element.getDataType();
        return DQLDataType.doesNotSatisfy(returned, allowed)
                && !allowed.contains(DQLDataType.ANY)
                && !returned.contains(DQLDataType.ANY);
    }

    protected boolean hasOnlyStaticValues(List<DQLExpression> expressions) {
        for (DQLExpression expression : expressions) {
            if (!hasStaticValue(expression)) {
                return false;
            }
        }
        return true;
    }

    protected boolean hasStaticValue(PsiElement toValidate) {
        return toValidate instanceof DQLSimpleExpression;
    }

    protected void validateExpressionOperands(@NotNull ProblemsHolder holder, @Nullable DQLExpression expression, @NotNull Set<Class<?>> allowedExpressions) {
        if (expression == null) {
            return;
        }
        if (!isTargetInvalid(expression, allowedExpressions)) {
            holder.registerProblem(expression, DQLBundle.message(
                    "inspection.expression.operator.target.invalid",
                    DQLBundle.shorten(expression.getText())
            ));
        }
    }

    protected List<PsiElement> getInvalidValues(DQLExpression expression, ParameterValidator validator) {
        List<PsiElement> toValidate = new ArrayList<>();
        toValidate.add(expression);
        List<PsiElement> invalidElements = new ArrayList<>();

        while (!toValidate.isEmpty()) {
            PsiElement element = toValidate.removeFirst();
            if (element instanceof DQLParameterExpression param) {
                toValidate.add(param.getExpression());
                continue;
            }
            if (element instanceof DQLParenthesisedExpression paren) {
                toValidate.add(DQLUtil.unpackParenthesis(paren));
                continue;
            }
            if (element instanceof DQLBracketExpression bracketExpression) {
                toValidate.addAll(bracketExpression.getExpressionList());
                continue;
            }

            if (element != null && validator.isElementInvalid(element)) {
                invalidElements.add(element);
            }
        }

        return invalidElements;
    }

    protected @NotNull Map<BaseElement, Set<DQLDataType>> findInvalidSidesForExpression(@NotNull TwoSidesExpression expression) {
        DQLOperationTarget targetType = DQLOperationsLoader.getTargetType(expression.getOperator());
        if (targetType != null) {
            DQLExpression left = expression.getLeftExpression();
            DQLExpression right = expression.getRightExpression();
            if (left instanceof BaseElement leftEl && right instanceof BaseElement rightEl) {
                return targetType.getInvalidSides(leftEl, rightEl);
            }
        }
        return Map.of();
    }
}
