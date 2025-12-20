package pl.thedeem.intellij.dql.inspections.parameters;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.inspections.fixes.JoinParameterGroupsQuickFix;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLFunctionExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

import java.util.List;

public class MultipleParameterGroupsInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitCommand(@NotNull DQLCommand statement) {
                super.visitCommand(statement);
                List<MappedParameter> parameters = statement.getParameters();
                validateParameterGroups(parameters, holder);
            }

            @Override
            public void visitFunctionExpression(@NotNull DQLFunctionExpression function) {
                super.visitFunctionExpression(function);
                List<MappedParameter> parameters = function.getParameters();
                validateParameterGroups(parameters, holder);
            }
        };
    }

    private void validateParameterGroups(@NotNull List<MappedParameter> parameters, @NotNull ProblemsHolder holder) {
        for (MappedParameter parameter : parameters) {
            List<List<PsiElement>> groups = parameter.getParameterGroups();
            if (groups.size() > 1) {
                holder.registerProblem(
                        parameter.holder(),
                        DQLBundle.message(
                                "inspection.parameter.multipleParameterGroups.groupDetected",
                                parameter.name()
                        ),
                        new JoinParameterGroupsQuickFix(parameter)
                );
            }
        }
    }
}
