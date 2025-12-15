package pl.thedeem.intellij.dql.inspections.parameters.types;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class AbstractParameterValueTypeInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);
                if (expression.getParent() instanceof DQLParametersOwner owner) {
                    MappedParameter parameter = owner.getParameter(expression);
                    Parameter definition = parameter != null ? parameter.definition() : null;
                    if (definition == null) {
                        return;
                    }
                    List<String> parameterTypes = Objects.requireNonNullElse(definition.parameterValueTypes(), List.of());
                    validateParameterValueType(expression, parameter, definition, parameterTypes, holder);
                }
            }
        };
    }

    protected abstract void validateParameterValueType(
            @NotNull DQLExpression expression,
            @NotNull MappedParameter parameter,
            @NotNull Parameter definition,
            @NotNull List<String> parameterTypes,
            @NotNull ProblemsHolder holder
    );

    protected List<PsiElement> getInvalidValues(DQLExpression expression, Predicate<PsiElement> validator) {
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

            if (element != null && validator.test(element) && doesNotContainErrorToken(element)) {
                invalidElements.add(element);
            }
        }

        return invalidElements;
    }
}
