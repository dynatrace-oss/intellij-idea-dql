package pl.thedeem.intellij.dql.inspections.fields;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.inspections.fixes.DropElementQuickFix;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;
import pl.thedeem.intellij.dql.services.parameters.model.MappedParameter;
import pl.thedeem.intellij.dql.services.query.DQLFieldsCalculatorService;

import java.util.List;

public class AliasDisallowedInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);
                if (!(expression instanceof DQLParametersOwner parametersOwner)) {
                    return;
                }
                validateParameters(parametersOwner, holder);
            }

            @Override
            public void visitCommand(@NotNull DQLCommand command) {
                super.visitCommand(command);
                validateParameters(command, holder);
            }
        };
    }

    private void validateParameters(@NotNull DQLParametersOwner parametersOwner, @NotNull ProblemsHolder holder) {
        for (MappedParameter parameter : parametersOwner.getParameters()) {
            if (parameter.holder() instanceof DQLParameterExpression named && "alias".equalsIgnoreCase(named.getName())) {
                validateAliasParameter(named, parameter, holder);
            }
        }
    }

    private void validateAliasParameter(@NotNull DQLParameterExpression alias, @NotNull MappedParameter parameter, @NotNull ProblemsHolder holder) {
        DQLFieldsCalculatorService service = DQLFieldsCalculatorService.getInstance();
        List<DQLFieldsCalculatorService.MappedField> parameters = service.calculateDefinedFields(parameter);
        if (parameters.stream().noneMatch(p -> p.nameExpression() == alias)) {
            DQLExpression expression = alias.getExpression();
            holder.registerProblem(
                    alias,
                    DQLBundle.message("inspection.command.invalidAlias.cannotBeNamed", expression != null ? expression.getText() : ""),
                    new DropElementQuickFix()
            );
        }
    }
}
