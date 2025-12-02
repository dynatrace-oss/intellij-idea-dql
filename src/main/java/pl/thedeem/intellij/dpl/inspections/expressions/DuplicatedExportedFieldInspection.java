package pl.thedeem.intellij.dpl.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.inspections.fixes.DropExportNameQuickFix;
import pl.thedeem.intellij.dpl.inspections.fixes.ReplaceExportNameQuickFix;
import pl.thedeem.intellij.dpl.psi.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicatedExportedFieldInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitDpl(@NotNull DPLDpl dpl) {
                super.visitDpl(dpl);
                validateExpressionsList(dpl.getExpressionsSequence());
            }

            @Override
            public void visitExpressionsSequence(@NotNull DPLExpressionsSequence sequence) {
                super.visitExpressionsSequence(sequence);
                validateExpressionsList(sequence);
            }

            private void validateExpressionsList(@Nullable DPLExpressionsSequence sequence) {
                List<DPLExpressionDefinition> expressions = sequence != null ? sequence.getExpressionDefinitionList() : List.of();
                if (expressions.isEmpty()) {
                    return;
                }
                Set<String> usedNames = new HashSet<>();

                for (DPLExpressionDefinition expression : expressions) {
                    DPLFieldName exportedName = expression.getExportedName();
                    if (exportedName != null) {
                        String name = exportedName.getExportName().toLowerCase();
                        if (!usedNames.add(name)) {
                            holder.registerProblem(
                                    exportedName,
                                    DPLBundle.message("inspection.duplicatedExportedName", exportedName.getExportName()),
                                    new DropExportNameQuickFix(),
                                    new ReplaceExportNameQuickFix()
                            );
                        }
                    }
                }
            }
        };
    }
}
