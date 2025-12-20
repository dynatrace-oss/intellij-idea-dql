package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.inspections.fixes.SetFieldNameQuickFix;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

import java.util.ArrayList;
import java.util.List;

public class InvalidFieldReadOperationInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitCommand(@NotNull DQLCommand command) {
                super.visitCommand(command);
                validateParameters(command.getParameters(), holder);
            }

            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);

                if (!(expression instanceof DQLParametersOwner parametersOwner)) {
                    return;
                }

                validateParameters(parametersOwner.getParameters(), holder);
            }
        };
    }

    private void validateParameters(@NotNull List<MappedParameter> parameters, @NotNull ProblemsHolder holder) {
        for (MappedParameter parameter : parameters) {
            if (readonlyDisallowed(parameter) && parameter.definition() != null) {
                for (PsiElement value : parameter.getExpressions()) {
                    validateValue(value, holder);
                }
            }
        }
    }

    private void validateValue(@NotNull PsiElement expression, @NotNull ProblemsHolder holder) {
        List<PsiElement> toCheck = new ArrayList<>();
        toCheck.add(expression);
        while (!toCheck.isEmpty()) {
            PsiElement check = toCheck.removeFirst();
            if (check instanceof DQLBracketExpression bracket) {
                toCheck.addAll(bracket.getExpressionList());
            } else if (isReadExpression(check)) {
                holder.registerProblem(
                        check,
                        DQLBundle.message("inspection.fieldReadOperation.notAllowed"),
                        new SetFieldNameQuickFix()
                );
            }
        }
    }

    private boolean isReadExpression(PsiElement fieldExpression) {
        return !(fieldExpression instanceof DQLAssignExpression);
    }

    private boolean readonlyDisallowed(MappedParameter parameter) {
        Parameter definition = parameter != null ? parameter.definition() : null;
        if (definition == null) {
            return false;
        }
        return definition.requiresFieldName();
    }
}
