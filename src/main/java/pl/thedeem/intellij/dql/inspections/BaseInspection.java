package pl.thedeem.intellij.dql.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLSimpleExpression;

import java.util.List;

public abstract class BaseInspection extends LocalInspectionTool {
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

    protected boolean doesNotContainErrorToken(@NotNull PsiElement toValidate) {
        return PsiTreeUtil.findChildOfAnyType(toValidate, PsiErrorElement.class) == null;
    }
}
