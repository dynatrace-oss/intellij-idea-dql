package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLBracketExpression;
import pl.thedeem.intellij.dql.psi.DQLNegativeValueExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractParameterValidator implements ParameterValidator {
    protected List<PsiElement> getInvalidValues(@NotNull PsiElement expression, @NotNull Predicate<PsiElement> validator) {
        List<PsiElement> toValidate = new ArrayList<>();
        toValidate.add(expression);
        List<PsiElement> result = new ArrayList<>();

        while (!toValidate.isEmpty()) {
            PsiElement element = DQLUtil.unpackParenthesis(toValidate.removeFirst());
            switch (element) {
                case DQLParameterExpression param -> {
                    if (!StringUtil.equalsIgnoreCase("alias", param.getName())) {
                        toValidate.add(param.getExpression());
                    }
                }
                case DQLBracketExpression bracket -> toValidate.addAll(bracket.getExpressionList());
                case DQLAssignExpression assign -> toValidate.add(assign.getRightExpression());
                case DQLNegativeValueExpression negative -> toValidate.add(negative.getExpression());
                case null -> {
                }
                default -> {
                    if (validator.test(element)) {
                        result.add(element);
                    }
                }
            }
        }

        return result;
    }
}
