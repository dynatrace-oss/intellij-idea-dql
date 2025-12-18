package pl.thedeem.intellij.dql.definition.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.*;

import java.util.ArrayList;
import java.util.List;

public class FieldIdentifierValidator extends AbstractDefinitionValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        List<DQLParameterValueTypesValidator.ValueIssue> issues = new ArrayList<>();
        List<PsiElement> toProcess = new ArrayList<>();
        toProcess.add(parameter);
        while (!toProcess.isEmpty()) {
            PsiElement element = toProcess.removeFirst();
            if (element instanceof DQLBracketExpression bracket) {
                toProcess.addAll(bracket.getExpressionList());
            } else if (element instanceof DQLParameterExpression param) {
                toProcess.add(param.getExpression());
            } else if (element instanceof DQLAssignExpression assign) {
                toProcess.add(assign.getLeftExpression());
            } else if (element instanceof DQLArrayExpression array) {
                toProcess.add(array.getLeftExpression());
            } else if (
                    !(element instanceof DQLString && definition.parameterValueTypes().contains("dql.parameterValueType.fieldPattern")) &&
                            !(element instanceof DQLSimpleExpression && definition.parameterValueTypes().contains("dql.parameterValueType.primitiveValue")) &&
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
