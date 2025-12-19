package pl.thedeem.intellij.dql.inspections.functions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.definition.model.Signature;
import pl.thedeem.intellij.dql.inspections.fixes.AddMissingParametersQuickFix;
import pl.thedeem.intellij.dql.psi.DQLFunctionExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class MissingFunctionParametersInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitFunctionExpression(@NotNull DQLFunctionExpression function) {
                super.visitFunctionExpression(function);

                Function definition = function.getDefinition();
                if (definition == null) {
                    return;
                }

                Collection<Parameter> missingParameters = function.getMissingRequiredParameters();
                if (!missingParameters.isEmpty() && !isEmptyAllowedForVariadic(function)) {
                    List<LocalQuickFix> fixes = missingParameters.stream()
                            .map(p -> (LocalQuickFix) new AddMissingParametersQuickFix(
                                    List.of(p),
                                    function.getTextRange().getEndOffset() - 1,
                                    !function.getExpressionList().isEmpty()))
                            .toList();

                    holder.registerProblem(
                            function.getFunctionName(),
                            DQLBundle.message("inspection.function.parameters.missingRequired",
                                    DQLBundle.print(missingParameters.stream().map(Parameter::name).toList())
                            ),
                            fixes.toArray(new LocalQuickFix[0])
                    );
                }
            }
        };
    }

    // In DQL, you can create an empty variadic list if it's the only parameter in the function
    private boolean isEmptyAllowedForVariadic(@NotNull DQLFunctionExpression function) {
        Signature signature = function.getSignature();
        if (signature == null || !function.getParameters().isEmpty()) {
            return false;
        }
        List<Parameter> parameters = Objects.requireNonNullElse(signature.parameters(), List.of());
        return parameters.stream().filter(p -> p.variadic() && !p.requiresName() && p.required()).toList().size() == 1;
    }
}
