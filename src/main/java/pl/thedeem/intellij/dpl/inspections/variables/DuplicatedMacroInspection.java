package pl.thedeem.intellij.dpl.inspections.variables;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.indexing.references.DPLVariableReference;
import pl.thedeem.intellij.dpl.inspections.fixes.DropMacroQuickFix;
import pl.thedeem.intellij.dpl.inspections.fixes.ReplaceMacroNameQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLMacroDefinitionExpression;
import pl.thedeem.intellij.dpl.psi.DPLVariable;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public class DuplicatedMacroInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitMacroDefinitionExpression(@NotNull DPLMacroDefinitionExpression macro) {
                super.visitMacroDefinitionExpression(macro);

                DPLVariable variable = macro.getVariable();

                PsiReference[] references = variable.getReferences();
                for (PsiReference reference : references) {
                    if (reference instanceof DPLVariableReference variableReference) {
                        ResolveResult[] results = variableReference.multiResolve(true);
                        if (results.length > 1) {
                            holder.registerProblem(
                                    variable,
                                    DPLBundle.message("inspection.variable.macroDuplicated", variable.getName()),
                                    new DropMacroQuickFix(),
                                    new ReplaceMacroNameQuickFix()
                            );
                        }
                        break;
                    }
                }
            }
        };
    }
}
