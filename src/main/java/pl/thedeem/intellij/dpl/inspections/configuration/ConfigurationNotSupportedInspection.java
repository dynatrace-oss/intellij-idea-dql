package pl.thedeem.intellij.dpl.inspections.configuration;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.inspections.fixes.DropConfigurationQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLConfigurationExpression;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

import java.util.Map;
import java.util.Objects;

public class ConfigurationNotSupportedInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitExpressionDefinition(@NotNull DPLExpressionDefinition expression) {
                super.visitExpressionDefinition(expression);

                DPLConfigurationExpression configuration = expression.getConfiguration();
                if (configuration == null) {
                    return;
                }
                ExpressionDescription description = expression.getDefinition();
                Map<String, Configuration> configurationDefinition = description != null ?
                        Objects.requireNonNullElse(description.configuration(), Map.of())
                        : Map.of();
                if (configurationDefinition.isEmpty()) {
                    holder.registerProblem(
                            configuration.getConfigurationContent(),
                            DPLBundle.message("inspection.configurationNotAllowed"),
                            new DropConfigurationQuickFix()
                    );
                }
            }
        };
    }
}
