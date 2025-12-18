package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.DQLDurationType;
import pl.thedeem.intellij.dql.psi.DQLTimeAlignmentOperand;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

import java.util.regex.Pattern;

public class InvalidTimeAlignmentDurationInspection extends LocalInspectionTool {
    private static final Pattern VALID_DURATION_UNIT = Pattern.compile(
            String.join("|", DQLDurationType.getTypes()) + "|w[0-7]?"
    );

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitTimeAlignmentOperand(@NotNull DQLTimeAlignmentOperand operand) {
                super.visitTimeAlignmentOperand(operand);

                if (!VALID_DURATION_UNIT.matcher(operand.getText()).matches()) {
                    holder.registerProblem(operand, DQLBundle.message(
                            "inspection.time.alignment.operand.type.invalid",
                            DQLBundle.print(DQLDurationType.getTypes())
                    ));
                }
            }
        };
    }
}