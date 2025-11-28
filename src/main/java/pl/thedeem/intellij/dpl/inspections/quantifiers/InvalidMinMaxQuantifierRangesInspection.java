package pl.thedeem.intellij.dpl.inspections.quantifiers;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.*;

import java.util.List;

public class InvalidMinMaxQuantifierRangesInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitMinMaxQuantifier(@NotNull DPLMinMaxQuantifier quantifier) {
                super.visitMinMaxQuantifier(quantifier);
                List<DPLQuantifierLimit> ranges = quantifier.getQuantifierLimitList();
                if (ranges.size() != 2) {
                    return;
                }
                Long min = ranges.getFirst().getLongValue();
                Long max = ranges.getLast().getLongValue();

                if (min > max) {
                    holder.registerProblem(
                            quantifier,
                            DPLBundle.message("inspection.command.invalidMinMaxQuantifierRanges")
                    );
                }
            }
        };
    }
}
