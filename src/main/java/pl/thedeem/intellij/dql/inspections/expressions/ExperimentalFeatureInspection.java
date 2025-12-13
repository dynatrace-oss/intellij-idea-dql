package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.*;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

public class ExperimentalFeatureInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitQueryStatement(@NotNull DQLQueryStatement command) {
                super.visitQueryStatement(command);

                Command definition = command.getDefinition();
                if (definition == null) {
                    return;
                }
                if (definition.experimental()) {
                    holder.registerProblem(
                            command,
                            DQLBundle.message("inspection.experimentalFeature.command", definition.name()),
                            ProblemHighlightType.WEAK_WARNING
                    );
                }
            }

            @Override
            public void visitFunctionCallExpression(@NotNull DQLFunctionCallExpression function) {
                super.visitFunctionCallExpression(function);

                Function definition = function.getDefinition();
                Signature signature = function.getSignature();
                if (definition == null) {
                    return;
                }
                if (definition.experimental() || (signature != null && signature.experimental())) {
                    holder.registerProblem(
                            function,
                            DQLBundle.message("inspection.experimentalFeature.function", definition.name()),
                            ProblemHighlightType.WEAK_WARNING
                    );
                }
            }

            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);

                if (expression.getParent() instanceof DQLParametersOwner parametersOwner) {
                    MappedParameter parameter = parametersOwner.getParameter(expression);
                    Parameter definition = parameter != null ? parameter.definition() : null;
                    if (definition == null) {
                        return;
                    }
                    if (definition.experimental()) {
                        holder.registerProblem(
                                expression,
                                DQLBundle.message("inspection.experimentalFeature.parameter", definition.name()),
                                ProblemHighlightType.WEAK_WARNING
                        );
                    }
                }
            }
        };
    }
}
