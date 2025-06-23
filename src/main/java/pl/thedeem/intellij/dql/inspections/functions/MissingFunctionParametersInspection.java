package pl.thedeem.intellij.dql.inspections.functions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.inspections.fixes.AddMissingParametersQuickFix;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

import java.util.List;

public class MissingFunctionParametersInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitFunctionCallExpression(@NotNull DQLFunctionCallExpression function) {
                super.visitFunctionCallExpression(function);

                List<DQLParameterDefinition> missingRequiredParameters = function.getMissingRequiredParameters();
                if (!missingRequiredParameters.isEmpty()) {
                    if (function.getDefinition() != null && function.getDefinition().getParameters(function).size() == 1) {
                        DQLParameterDefinition first = missingRequiredParameters.getFirst();
                        if (first.repetitive) {
                            return; // for some reason, DQL allows empty required parameters when it is repetitive and the only one
                        }
                    }
                    holder.registerProblem(
                            function.getFunctionName(),
                            DQLBundle.message("inspection.function.parameters.missingRequired",
                                    DQLBundle.print(missingRequiredParameters.stream().map(p -> p.name).toList())
                            ),
                            new AddMissingParametersQuickFix(missingRequiredParameters, function.getTextRange().getEndOffset() - 1, !function.getExpressionList().isEmpty())
                    );
                }
            }
        };
    }
}
