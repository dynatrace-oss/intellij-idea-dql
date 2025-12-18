package pl.thedeem.intellij.dql.inspections.simple;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.DQLDurationType;
import pl.thedeem.intellij.dql.psi.DQLDuration;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class DurationTypeInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {

            @Override
            public void visitDuration(@NotNull DQLDuration duration) {
                super.visitDuration(duration);

                DQLDurationType durationType = duration.getDurationType();
                if (durationType == null) {
                    holder.registerProblem(duration, DQLBundle.message(
                            "inspection.duration.type.invalid",
                            DQLBundle.print(DQLDurationType.getTypes())
                    ));
                }
            }
        };
    }
}
