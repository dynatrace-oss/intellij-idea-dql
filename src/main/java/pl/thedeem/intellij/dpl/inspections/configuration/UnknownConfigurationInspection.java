package pl.thedeem.intellij.dpl.inspections.configuration;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.inspections.fixes.DropConfigurationParameterQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLConfiguration;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLParameter;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

import java.util.Map;

public class UnknownConfigurationInspection extends LocalInspectionTool {
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
                Map<String, Configuration> configurationDefinition = expression.getConfigurationDefinition();
                if (configurationDefinition == null) {
                    return;
                }

                for (DPLParameter definedParameter : configuration.getParameterList()) {
                    String parameterName = definedParameter.getParameterName().getName();
                    if (!configurationDefinition.containsKey(parameterName)) {
                        holder.registerProblem(
                                definedParameter,
                                DPLBundle.message("inspection.parameterUnknown", parameterName),
                                new DropConfigurationParameterQuickFix()
                        );
                    }
                }
            }
        };
    }
}
