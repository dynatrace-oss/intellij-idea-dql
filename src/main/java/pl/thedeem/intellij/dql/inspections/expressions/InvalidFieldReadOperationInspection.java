package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.inspections.fixes.SetFieldNameQuickFix;
import pl.thedeem.intellij.dql.psi.*;

import java.util.List;

public class InvalidFieldReadOperationInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitFieldExpression(@NotNull DQLFieldExpression expression) {
                super.visitFieldExpression(expression);

                if (isReadExpression(expression) && !isReadingFieldValueAllowed(expression)) {
                    holder.registerProblem(
                            expression,
                            DQLBundle.message("inspection.fieldReadOperation.notAllowed"),
                            new SetFieldNameQuickFix()
                    );
                }
            }
        };
    }

    private boolean isReadExpression(DQLFieldExpression fieldExpression) {
        return !(fieldExpression.getParent() instanceof DQLAssignExpression);
    }

    private boolean isReadingFieldValueAllowed(DQLFieldExpression fieldExpression) {
        List<PsiElement> parentsUntil = PsiUtils.getElementsUntilParent(fieldExpression, DQLFunctionCallExpression.class, DQLQueryStatement.class);
        if (!parentsUntil.isEmpty()) {
            PsiElement topParent = parentsUntil.getFirst();
            DQLExpression topParentChild = parentsUntil.get(1) instanceof DQLExpression expr ? expr : fieldExpression;
            if (topParent instanceof DQLFunctionCallExpression functionArgument) {
                return readonlyAllowed(functionArgument.getParameter(topParentChild));
            } else if (topParent instanceof DQLQueryStatement list) {
                return readonlyAllowed(list.getParameter(topParentChild));
            }
        }
        return true;
    }

    private boolean readonlyAllowed(MappedParameter parameter) {
        Parameter definition = parameter != null ? parameter.definition() : null;
        if (definition == null) {
            return true;
        }
        return !"mandatory".equals(definition.assignmentSupport());
    }
}
