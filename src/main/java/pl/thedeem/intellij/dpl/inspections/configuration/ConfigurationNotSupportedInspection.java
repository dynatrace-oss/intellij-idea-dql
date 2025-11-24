package pl.thedeem.intellij.dpl.inspections.configuration;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Command;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.inspections.fixes.DropConfigurationQuickFix;
import pl.thedeem.intellij.dpl.psi.*;

import java.util.Map;

public class ConfigurationNotSupportedInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitExpressionDefinition(@NotNull DPLExpressionDefinition expression) {
                super.visitExpressionDefinition(expression);

                DPLConfiguration configuration = expression.getConfiguration();
                if (configuration == null) {
                    return;
                }

                DPLExpression expr = expression.getExpression();
                if (expr instanceof DPLCommandExpression command) {
                    validateCommand(command, configuration, holder);
                }
            }
        };
    }

    private void validateCommand(@NotNull DPLCommandExpression command, @NotNull DPLConfiguration configuration, @NotNull ProblemsHolder holder) {
        Command definition = command.getDefinition();
        if (definition == null) {
            return;
        }
        Map<String, Configuration> commandConfiguration = definition.configuration();

        if (commandConfiguration == null) {
            holder.registerProblem(
                    configuration,
                    DPLBundle.message("inspection.command.configurationNotAllowed", command.getName()),
                    new DropConfigurationQuickFix()
            );
        }
    }
}
