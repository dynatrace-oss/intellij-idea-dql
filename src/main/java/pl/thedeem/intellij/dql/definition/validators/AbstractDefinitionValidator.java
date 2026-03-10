package pl.thedeem.intellij.dql.definition.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLBracketExpression;
import pl.thedeem.intellij.dql.psi.DQLNegativeValueExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractDefinitionValidator {
    public abstract @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition);

    protected List<PsiElement> getInvalidValues(@NotNull PsiElement expression, @NotNull Predicate<PsiElement> validator) {
        List<PsiElement> toValidate = new ArrayList<>();
        toValidate.add(expression);
        List<PsiElement> invalidElements = new ArrayList<>();

        while (!toValidate.isEmpty()) {
            PsiElement element = DQLUtil.unpackParenthesis(toValidate.removeFirst());
            switch (element) {
                case DQLParameterExpression param -> toValidate.add(param.getExpression());
                case DQLBracketExpression bracket -> toValidate.addAll(bracket.getExpressionList());
                case DQLAssignExpression assign -> toValidate.add(assign.getRightExpression());
                case DQLNegativeValueExpression negative -> toValidate.add(negative.getExpression());
                case null -> {
                }
                default -> {
                    if (validator.test(element)) {
                        invalidElements.add(element);
                    }
                }
            }
        }

        return invalidElements;
    }
}
