package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.*;

import java.util.List;

public class InvalidFieldWriteOperationInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitAssignExpression(@NotNull DQLAssignExpression expression) {
                super.visitAssignExpression(expression);

                if (isWritingFieldValueInvalid(expression)) {
                    holder.registerProblem(
                            expression,
                            DQLBundle.message("inspection.fieldWriteOperation.notAllowed")
                    );
                }
            }
        };
    }

    private boolean isWritingFieldValueInvalid(DQLAssignExpression expression) {
        List<PsiElement> parentsUntil = DQLUtil.getElementsUntilParent(expression, DQLFunctionCallExpression.class, DQLQueryStatement.class);
        if (!parentsUntil.isEmpty()) {
            PsiElement topParent = parentsUntil.getFirst();
            DQLExpression topParentChild = parentsUntil.get(1) instanceof DQLExpression expr ? expr : expression;

            if (topParent instanceof DQLFunctionCallExpression functionArgument) {
                return assignmentNotAllowed(functionArgument.getParameter(topParentChild));
            } else if (topParent instanceof DQLQueryStatement list) {
                return assignmentNotAllowed(list.getParameter(topParentChild));
            }
        }
        return true;
    }

    private boolean assignmentNotAllowed(DQLParameterObject parameter) {
        if (parameter != null && parameter.getDefinition() != null) {
            DQLParameterDefinition definition = parameter.getDefinition();
            if (definition.satisfies(DQLDataType.READ_ONLY_EXPRESSION)) {
                return true;
            }
            return DQLDataType.doesNotSatisfy(DQLDataType.ASSIGN_VALUE_TYPES, definition.getDQLTypes());
        }
        return true;
    }
}
