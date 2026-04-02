package pl.thedeem.intellij.dql.inspections.fields;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.inspections.fixes.SetFieldNameQuickFix;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.model.MappedParameter;
import pl.thedeem.intellij.dql.services.query.DQLFieldsCalculatorService;

import java.util.Collection;
import java.util.List;

public class InvalidFieldReadOperationInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitCommand(@NotNull DQLCommand command) {
                super.visitCommand(command);
                validateParameters(command.getParameters(), holder);
            }

            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);

                if (!(expression instanceof DQLParametersOwner parametersOwner)) {
                    return;
                }

                validateParameters(parametersOwner.getParameters(), holder);
            }
        };
    }

    private void validateParameters(@NotNull List<MappedParameter> parameters, @NotNull ProblemsHolder holder) {
        DQLFieldsCalculatorService service = DQLFieldsCalculatorService.getInstance();

        for (MappedParameter parameter : parameters) {
            if (parameter.definition() != null && readonlyDisallowed(parameter)) {
                Collection<DQLFieldsCalculatorService.MappedField> definedFields = service.calculateDefinedFields(parameter);

                for (DQLFieldsCalculatorService.MappedField definedField : definedFields) {
                    if (definedField.nameExpression() == null) {
                        holder.registerProblem(
                                definedField.expression(),
                                DQLBundle.message("inspection.fieldReadOperation.notAllowed"),
                                new SetFieldNameQuickFix()
                        );
                    }
                }

            }
        }
    }

    private boolean readonlyDisallowed(MappedParameter parameter) {
        Parameter definition = parameter != null ? parameter.definition() : null;
        if (definition == null) {
            return false;
        }
        return definition.requiresFieldName();
    }
}
