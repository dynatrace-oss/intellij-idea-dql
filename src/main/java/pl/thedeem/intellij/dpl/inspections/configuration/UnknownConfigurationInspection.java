package pl.thedeem.intellij.dpl.inspections.configuration;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.inspections.fixes.DropConfigurationParameterQuickFix;
import pl.thedeem.intellij.dpl.psi.*;

import java.util.Objects;

public class UnknownConfigurationInspection extends LocalInspectionTool {
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
                ExpressionDescription definition = expression.getDefinition();
                if (definition == null || definition.configuration() == null) {
                    return;
                }
                for (DPLParameterExpression definedParameter : configuration.getConfigurationContent().getParameterExpressionList()) {
                    DPLParameterName paramName = definedParameter.getParameterName();
                    if (paramName != null) {
                        String parameterName = Objects.requireNonNullElse(paramName.getName(), "").toLowerCase();
                        Configuration parameterDefinition = expression.getParameterDefinition(parameterName);
                        if (parameterDefinition == null) {
                            holder.registerProblem(
                                    definedParameter,
                                    DPLBundle.message("inspection.parameterUnknown", parameterName),
                                    new DropConfigurationParameterQuickFix()
                            );
                        }
                    }
                }
            }
        };
    }

}
