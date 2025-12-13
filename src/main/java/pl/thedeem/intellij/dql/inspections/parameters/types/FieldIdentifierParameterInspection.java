package pl.thedeem.intellij.dql.inspections.parameters.types;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.*;

import java.util.ArrayList;
import java.util.List;

public class FieldIdentifierParameterInspection extends AbstractParameterValueTypeInspection {
    @Override
    protected void validateParameterValueType(
            @NotNull DQLExpression expression,
            @NotNull MappedParameter parameter,
            @NotNull Parameter definition,
            @NotNull List<String> parameterTypes,
            @NotNull ProblemsHolder holder) {
        if (parameterTypes.stream().noneMatch(DQLDefinitionService.FIELD_IDENTIFIER_PARAMETER_VALUE_TYPES::contains)) {
            return;
        }
        List<PsiElement> toProcess = new ArrayList<>();
        toProcess.add(parameter.holder());
        toProcess.addAll(parameter.included());

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
            } else if (!(element instanceof DQLFieldExpression)) {
                if (!(element instanceof DQLString && parameterTypes.contains("dql.parameterValueType.fieldPattern"))) {
                    holder.registerProblem(
                            element,
                            DQLBundle.message("inspection.parameter.fieldIdentifier.notAField", definition.name())
                    );
                }
            }
        }
    }
}
