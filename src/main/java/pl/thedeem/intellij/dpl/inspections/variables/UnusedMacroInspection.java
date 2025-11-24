package pl.thedeem.intellij.dpl.inspections.variables;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.inspections.fixes.DropMacroQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLMacroDefinitionExpression;
import pl.thedeem.intellij.dpl.psi.DPLVariable;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

import java.util.Collection;

public class UnusedMacroInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitMacroDefinitionExpression(@NotNull DPLMacroDefinitionExpression macro) {
                super.visitMacroDefinitionExpression(macro);

                DPLVariable variable = macro.getVariable();

                Collection<PsiReference> all = ReferencesSearch.search(variable).findAll();
                if (all.size() == 1) {
                    holder.registerProblem(
                            variable,
                            DPLBundle.message("inspection.variable.unused", variable.getName()),
                            ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                            new DropMacroQuickFix()
                    );
                }
            }
        };
    }
}
