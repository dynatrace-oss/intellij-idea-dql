package pl.thedeem.intellij.dql.inspections.functions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.inspections.fixes.DropInvalidParameterQuickFix;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

import java.util.HashSet;
import java.util.Set;

public class DuplicatedFunctionParameterInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitFunctionCallExpression(@NotNull DQLFunctionCallExpression function) {
                super.visitFunctionCallExpression(function);

                Set<String> alreadyProcessed = new HashSet<>();
                for (MappedParameter parameter : function.getParameters()) {
                    Parameter definition = parameter.definition();
                    if (definition != null && !definition.variadic() && !alreadyProcessed.add(definition.name())) {
                        holder.registerProblem(
                                parameter.holder(),
                                DQLBundle.message("inspection.function.duplicatedParameters.duplicated", definition.name()),
                                new DropInvalidParameterQuickFix(parameter)
                        );
                    }
                }
            }
        };
    }
}
