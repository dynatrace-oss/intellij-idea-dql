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
import pl.thedeem.intellij.dql.inspections.parameters.parameterValidators.JoinConditionValidator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

public class JoinConditionParameterInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);
                if (expression.getParent() instanceof DQLParametersOwner command) {
                    DQLParameterObject parameter = command.getParameter(expression);
                    if (parameter == null) {
                        return;
                    }
                    DQLParameterDefinition definition = parameter.getDefinition();
                    if (definition == null || !definition.getDQLTypes().contains(DQLDataType.JOIN_CONDITION)) {
                        return;
                    }

                    for (PsiElement invalidValue : getInvalidValues(expression, new JoinConditionValidator())) {
                        holder.registerProblem(
                                invalidValue,
                                DQLBundle.message("inspection.parameter.joinCondition.invalidCondition", definition.name)
                        );
                    }
                }
            }
        };
    }
}
