package pl.thedeem.intellij.dpl.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.inspections.fixes.ReorderElementDefinitionQuickFix;
import pl.thedeem.intellij.dpl.psi.*;

import java.util.ArrayList;
import java.util.List;

public class InvalidModifierOrderInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitExpressionDefinition(@NotNull DPLExpressionDefinition expression) {
                super.visitExpressionDefinition(expression);
                
                DPLMatchersExpression matchers = expression.getMatchers();
                DPLLookaroundExpression lookaround = expression.getLookaround();
                DPLDefinitionExpression definedExpression = expression.getDefinedExpression();
                DPLConfigurationExpression configuration = expression.getConfiguration();
                DPLQuantifierExpression quantifier = expression.getQuantifier();
                DPLNullableExpression nullable = expression.getNullable();
                DPLFieldName memberName = expression.getMemberName();
                DPLFieldName exportedName = expression.getExportedName();

                List<PsiElement> ranges = new ArrayList<>();
                if (lookaround != null) {
                    ranges.add(lookaround.getLookaround());
                }
                if (definedExpression != null) {
                    ranges.add(definedExpression);
                }
                if (matchers != null && matchers.getCommandMatchersContent() != null) {
                    ranges.add(matchers.getCommandMatchersContent());
                }
                if (configuration != null) {
                    ranges.add(configuration.getConfigurationContent());
                }
                if (quantifier != null) {
                    ranges.add(quantifier.getQuantifierContent());
                }
                if (nullable != null) {
                    ranges.add(nullable.getNullable());
                }
                if (memberName != null) {
                    ranges.add(memberName);
                }
                if (exportedName != null) {
                    ranges.add(exportedName);
                }
                List<PsiElement> invalidModifiers = getInvalidModifiers(ranges);

                for (PsiElement invalidModifier : invalidModifiers) {
                    holder.registerProblem(
                            invalidModifier,
                            DPLBundle.message("inspection.invalidModifierOrder"),
                            new ReorderElementDefinitionQuickFix()
                    );
                }
            }
        };
    }

    private List<PsiElement> getInvalidModifiers(List<PsiElement> elements) {
        List<PsiElement> invalidModifiers = new ArrayList<>();
        for (int i = 1; i < elements.size(); i++) {
            PsiElement previous = elements.get(i - 1);
            PsiElement current = elements.get(i);
            if (previous.getTextRange().getStartOffset() > current.getTextRange().getStartOffset()) {
                invalidModifiers.add(current);
            }
        }
        return invalidModifiers;
    }
}
