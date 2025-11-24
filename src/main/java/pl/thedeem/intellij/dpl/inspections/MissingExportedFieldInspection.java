package pl.thedeem.intellij.dpl.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLDpl;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

import java.util.List;

public class MissingExportedFieldInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitDpl(@NotNull DPLDpl dpl) {
                super.visitDpl(dpl);

                List<DPLExpressionDefinition> expressions = dpl.getExpressionDefinitionList();
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
