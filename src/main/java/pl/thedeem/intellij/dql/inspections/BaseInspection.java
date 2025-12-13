package pl.thedeem.intellij.dql.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLSimpleExpression;

import java.util.List;
import java.util.Set;

public abstract class BaseInspection extends LocalInspectionTool {
    protected boolean isTargetInvalid(DQLExpression expression, Set<Class<?>> allowedExpressions) {
        PsiElement unpacked = DQLUtil.unpackParenthesis(expression);
        return allowedExpressions.stream().anyMatch(t -> t.isInstance(unpacked));
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
        if (!isTargetInvalid(expression, allowedExpressions) && doesNotContainErrorToken(expression)) {
            holder.registerProblem(expression, DQLBundle.message(
                    "inspection.expression.operator.target.invalid",
                    DQLBundle.shorten(expression.getText())
            ));
        }
    }

    protected boolean doesNotContainErrorToken(@NotNull PsiElement toValidate) {
        return PsiTreeUtil.findChildOfAnyType(toValidate, PsiErrorElement.class) == null;
    }
}
