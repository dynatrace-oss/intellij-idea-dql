package pl.thedeem.intellij.dpl.inspections.configuration;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.inspections.fixes.DropConfigurationParameterQuickFix;
import pl.thedeem.intellij.dpl.psi.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DuplicatedConfigurationInspection extends LocalInspectionTool {
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

                Set<String> usedParameters = new HashSet<>();
                for (DPLParameterExpression definedParameter : configuration.getConfigurationContent().getParameterExpressionList()) {
                    DPLParameterName paramName = definedParameter.getParameterName();
                    if (paramName != null) {
                        String parameterName = Objects.requireNonNullElse(paramName.getName(), "").toLowerCase();
                        Configuration parameterDefinition = expression.getParameterDefinition(parameterName);
                        if (parameterDefinition != null && !usedParameters.add(parameterDefinition.name())) {
                            holder.registerProblem(
                                    definedParameter,
                                    DPLBundle.message("inspection.duplicatedParameter", parameterName),
                                    new DropConfigurationParameterQuickFix()
                            );
                        }
                    }
                }
            }
        };
    }
}
