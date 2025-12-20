package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.inspections.fixes.DropElementQuickFix;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

import java.util.List;

public class InvalidFieldWriteOperationInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitAssignExpression(@NotNull DQLAssignExpression expression) {
                super.visitAssignExpression(expression);

                if (isWritingFieldValueInvalid(expression)) {
                    holder.registerProblem(
                            expression,
                            DQLBundle.message("inspection.fieldWriteOperation.notAllowed"),
                            new DropElementQuickFix()
                    );
                }
            }
        };
    }

    private boolean isWritingFieldValueInvalid(DQLAssignExpression expression) {
        List<PsiElement> parentsUntil = PsiUtils.getElementsUntilParent(expression, DQLParametersOwner.class);
        if (!parentsUntil.isEmpty()) {
            PsiElement topParent = parentsUntil.getFirst();
            DQLExpression topParentChild = parentsUntil.get(1) instanceof DQLExpression expr ? expr : expression;

            if (topParent instanceof DQLParametersOwner parametersOwner) {
                return assignmentNotAllowed(parametersOwner.getParameter(topParentChild));
            }
        }
        return true;
    }

    private boolean assignmentNotAllowed(@Nullable MappedParameter parameter) {
        Parameter definition = parameter != null ? parameter.definition() : null;
        if (definition == null) {
            return false;
        }
        return !definition.allowsFieldName();
    }
}
