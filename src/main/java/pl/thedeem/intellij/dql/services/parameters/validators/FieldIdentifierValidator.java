package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FieldIdentifierValidator extends AbstractParameterValidator {
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "dql.parameterValueType.fieldPattern",
            "dql.parameterValueType.identifierForFieldOnRootLevel",
            "dql.parameterValueType.identifierForAnyField",
            "dql.parameterValueType.dataObject"
    );

    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        if (definition.parameterValueTypes().stream().noneMatch(ALLOWED_TYPES::contains)) {
            return List.of();
        }

        List<DQLParameterValueTypesValidator.ValueIssue> issues = new ArrayList<>();
        List<PsiElement> toProcess = new ArrayList<>();
        toProcess.add(parameter);
        while (!toProcess.isEmpty()) {
            PsiElement element = toProcess.removeFirst();
            if (element == null) {
                continue;
            }
            if (element instanceof DQLBracketExpression bracket) {
                toProcess.addAll(bracket.getExpressionList());
            } else if (element instanceof DQLParameterExpression param) {
                toProcess.add(param.getExpression());
            } else if (element instanceof DQLAssignExpression assign) {
                toProcess.add(assign.getLeftExpression());
            } else if (element instanceof DQLArrayExpression array) {
                toProcess.add(array.getBaseExpression());
            } else if (element instanceof DQLNegativeValueExpression negative) {
                toProcess.add(negative.getExpression());
            } else if (
                    !(element instanceof DQLString && definition.parameterValueTypes().contains("dql.parameterValueType.fieldPattern")) &&
                            !(element instanceof DQLPrimitiveExpression && definition.parameterValueTypes().contains("dql.parameterValueType.primitiveValue")) &&
                            !(element instanceof DQLFieldExpression)) {
                issues.add(new DQLParameterValueTypesValidator.ValueIssue(
                        element,
                        DQLBundle.message("inspection.parameter.fieldIdentifier.notAField", definition.name())
                ));
            }
        }
        return issues;
    }
}
