package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.inspections.fixes.DropElementQuickFix;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

import java.util.List;

public class AliasDisallowedInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);
                if (!(expression instanceof DQLParametersOwner parametersOwner)) {
                    return;
                }
                validateParameters(parametersOwner.getParameters(), holder);
            }

            @Override
            public void visitCommand(@NotNull DQLCommand command) {
                super.visitCommand(command);
                validateParameters(command.getParameters(), holder);
            }
        };
    }

    private void validateParameters(@NotNull List<MappedParameter> parameters, @NotNull ProblemsHolder holder) {
        for (MappedParameter parameter : parameters) {
            if (!"alias".equalsIgnoreCase(parameter.name())) {
                List<PsiElement> expressions = parameter.unpackExpressions();
                for (int i = 0; i < expressions.size(); i++) {
                    PsiElement previous = i > 0 ? expressions.get(i - 1) : null;
                    PsiElement current = expressions.get(i);
                    if (current instanceof DQLParameterExpression named && "alias".equalsIgnoreCase(named.getName())) {
                        validateAliasParameter(named, previous, parameter, holder);
                    }
                }
            }
        }
    }

    private void validateAliasParameter(@NotNull DQLParameterExpression alias, @Nullable PsiElement previous, @NotNull MappedParameter parameter, @NotNull ProblemsHolder holder) {
        DQLExpression value = alias.getExpression();
        String valueText = value != null ? value.getText() : null;
        if (parameter.definition() == null || !parameter.definition().allowsFieldName()) {
            holder.registerProblem(
                    alias,
                    DQLBundle.message("inspection.command.invalidAlias.cannotBeNamed", valueText),
                    new DropElementQuickFix()
            );
            return;
        }
        if (!(value instanceof DQLFieldExpression)) {
            holder.registerProblem(
                    alias,
                    DQLBundle.message("inspection.command.invalidAlias.invalidValue"),
                    new DropElementQuickFix()
            );
            return;
        }
        if (previous == null || previous instanceof DQLParameterExpression named && "alias".equalsIgnoreCase(named.getName())) {
            holder.registerProblem(
                    alias,
                    DQLBundle.message("inspection.command.invalidAlias.noExpressionToBeAliased", valueText),
                    new DropElementQuickFix()
            );
            return;
        }
        if (DQLUtil.unpackParenthesis(previous) instanceof DQLAssignExpression assigned) {
            holder.registerProblem(
                    alias,
                    DQLBundle.message("inspection.command.invalidAlias.fieldAlreadyNamed", valueText, assigned.getFieldName()),
                    new DropElementQuickFix()
            );
        }
    }
}
