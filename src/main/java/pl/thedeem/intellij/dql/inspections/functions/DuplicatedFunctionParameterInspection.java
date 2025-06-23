package pl.thedeem.intellij.dql.inspections.functions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
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
                for (DQLParameterObject parameter : function.getParameters()) {
                    DQLParameterDefinition definition = parameter.getDefinition();
                    if (definition != null && !definition.isRepetitive() && !alreadyProcessed.add(definition.name)) {
                        holder.registerProblem(
                                parameter.getExpression(),
                                DQLBundle.message("inspection.function.duplicatedParameters.duplicated", definition.name),
                                new DropInvalidParameterQuickFix(parameter)
                        );
                    }
                }
            }
        };
    }
}
