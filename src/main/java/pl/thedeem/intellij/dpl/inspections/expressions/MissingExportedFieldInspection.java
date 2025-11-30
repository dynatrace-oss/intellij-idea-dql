package pl.thedeem.intellij.dpl.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLDpl;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLExpressionsSequence;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

import java.util.List;

public class MissingExportedFieldInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitDpl(@NotNull DPLDpl dpl) {
                super.visitDpl(dpl);

                DPLExpressionsSequence sequence = dpl.getExpressionsSequence();
                List<DPLExpressionDefinition> expressions = sequence != null ? sequence.getExpressionDefinitionList() : List.of();
                if (expressions.isEmpty()) {
                    return;
                }
                boolean exportFound = expressions.stream().anyMatch(e -> e.getExportedName() != null);

                if (!exportFound) {
                    holder.registerProblem(dpl, DPLBundle.message("inspection.missingExportedName"));
                }
            }
        };
    }
}
