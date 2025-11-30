package pl.thedeem.intellij.dpl.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.inspections.fixes.*;
import pl.thedeem.intellij.dpl.psi.*;
import pl.thedeem.intellij.dpl.psi.elements.ExpressionElement;

public class DuplicatedExpressionModifierInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitExpressionDefinition(@NotNull DPLExpressionDefinition expression) {
                super.visitExpressionDefinition(expression);

                ExpressionElement.ExpressionParts parts = expression.getExpressionParts();

                if (parts.configurations().size() > 1) {
                    for (DPLConfigurationContent content : parts.configurations().stream().map(DPLConfigurationExpression::getConfigurationContent).toList()) {
                        holder.registerProblem(
                                content,
                                DPLBundle.message("inspection.duplicatedExpressionModifier.configuration"),
                                new DropConfigurationQuickFix()
                        );
                    }
                }
                if (parts.quantifiers().size() > 1) {
                    for (DPLQuantifierContent content : parts.quantifiers().stream().map(DPLQuantifierExpression::getQuantifierContent).toList()) {
                        holder.registerProblem(
                                content,
                                DPLBundle.message("inspection.duplicatedExpressionModifier.quantifier"),
                                new DropQuantifierQuickFix()
                        );
                    }
                }
                if (parts.lookarounds().size() > 1) {
                    for (DPLLookaround lookaround : parts.lookarounds().stream().map(DPLLookaroundExpression::getLookaround).toList()) {
                        holder.registerProblem(
                                lookaround,
                                DPLBundle.message("inspection.duplicatedExpressionModifier.lookaround"),
                                new DropLookaroundQuickFix()
                        );
                    }
                }
                if (parts.matchers().size() > 1) {
                    for (DPLCommandMatchersContent content : parts.matchers().stream().map(DPLMatchersExpression::getCommandMatchersContent).toList()) {
                        if (content != null) {
                            holder.registerProblem(
                                    content,
                                    DPLBundle.message("inspection.duplicatedExpressionModifier.matchers"),
                                    new DropMatchersQuickFix()
                            );
                        }
                    }
                }
                if (parts.nullables().size() > 1) {
                    for (DPLNullable nullable : parts.nullables().stream().map(DPLNullableExpression::getNullable).toList()) {
                        holder.registerProblem(
                                nullable,
                                DPLBundle.message("inspection.duplicatedExpressionModifier.nullable"),
                                new DropNullableQuickFix()
                        );
                    }
                }
                int allowedNames = expression.isMembersListExpression() ? 2 : 1;
                if (parts.names().size() > allowedNames) {
                    for (DPLFieldName name : parts.names().stream().map(DPLExportNameExpression::getFieldName).toList()) {
                        holder.registerProblem(
                                name,
                                DPLBundle.message("inspection.duplicatedExpressionModifier.names"),
                                new DropExportNameQuickFix()
                        );
                    }
                }
            }
        };
    }
}
