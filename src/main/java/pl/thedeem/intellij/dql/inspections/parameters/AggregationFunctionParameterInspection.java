package pl.thedeem.intellij.dql.inspections.parameters;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.*;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.inspections.parameters.parameterValidators.ListOfFunctionsParameterValidator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.Set;

public class AggregationFunctionParameterInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);

                if (expression.getParent() instanceof DQLParametersOwner parametersOwner) {
                    DQLParameterObject parameter = parametersOwner.getParameter(expression);
                    DQLParameterDefinition definition = parameter != null ? parameter.getDefinition() : null;

                    if (definition == null || !definition.getDQLTypes().contains(DQLDataType.AGGREGATION_FUNCTION)) {
                        return;
                    }
                    DQLDefinitionService service = DQLDefinitionService.getInstance(expression.getProject());
                    Set<String> allowedFunctions = service.getFunctionNamesByGroups(Set.of(DQLFunctionGroup.AGGREGATE));
                    for (PsiElement invalidValue : getInvalidValues(expression, new ListOfFunctionsParameterValidator(allowedFunctions))) {
                        holder.registerProblem(
                                invalidValue,
                                DQLBundle.message(
                                        "inspection.parameter.aggregationFunction.invalidFunction",
                                        DQLBundle.print(allowedFunctions)
                                )
                        );
                    }
                }

            }
        };
    }
}
