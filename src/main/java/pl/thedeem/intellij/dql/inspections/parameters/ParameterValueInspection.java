package pl.thedeem.intellij.dql.inspections.parameters;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.inspections.parameters.parameterValidators.ParameterValueValidator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

public class ParameterValueInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);
                if (expression.getParent() instanceof DQLParametersOwner parametersOwner) {
                    DQLParameterObject parameter = parametersOwner.getParameter(expression);
                    if (parameter == null) {
                        return;
                    }
                    DQLParameterDefinition definition = parameter.getDefinition();
                    if (definition == null || definition.getDQLTypes().contains(DQLDataType.ANY)) {
                        return;
                    }
                    for (PsiElement invalidValue : getInvalidValues(expression, new ParameterValueValidator(definition.getDQLTypes()))) {
                        holder.registerProblem(
                                invalidValue,
                                DQLBundle.message(
                                        "inspection.command.parametersValue.invalidValue",
                                        definition.name,
                                        DQLBundle.print(definition.type),
                                        DQLBundle.print(DQLDataType.getTypes(parameter.getDataType()))
                                )
                        );
                    }
                }
            }
        };
    }
}
