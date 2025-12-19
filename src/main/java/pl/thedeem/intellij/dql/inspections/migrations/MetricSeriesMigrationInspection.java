package pl.thedeem.intellij.dql.inspections.migrations;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.inspections.fixes.ReplaceFetchWithMetricsQuickFix;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class MetricSeriesMigrationInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {

            @Override
            public void visitCommand(@NotNull DQLCommand command) {
                super.visitCommand(command);

                Command definition = command.getDefinition();
                if (definition == null || !"fetch".equalsIgnoreCase(definition.name())) {
                    return;
                }

                for (MappedParameter parameter : command.getParameters()) {
                    Parameter paramDefinition = parameter.definition();
                    if (paramDefinition != null && paramDefinition.parameterValueTypes() != null && paramDefinition.parameterValueTypes().contains("dql.parameterValueType.dataObject")) {
                        String paramValue = parameter.holder().getText();
                        if ("metric.series".equalsIgnoreCase(paramValue)) {
                            holder.registerProblem(
                                    command.getCommandKeyword(),
                                    DQLBundle.message("inspection.variable.metricSeriesMigration.issueDetected"),
                                    new ReplaceFetchWithMetricsQuickFix()
                            );
                        }
                    }
                }
            }
        };
    }
}
