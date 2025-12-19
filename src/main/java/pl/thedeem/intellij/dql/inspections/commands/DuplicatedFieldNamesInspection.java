package pl.thedeem.intellij.dql.inspections.commands;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.inspections.fixes.DropElementQuickFix;
import pl.thedeem.intellij.dql.inspections.fixes.SetFieldNameQuickFix;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;

import java.util.*;

public class DuplicatedFieldNamesInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);
                if (expression.getParent() instanceof DQLCommand command) {
                    MappedParameter parameter = command.getParameter(expression);
                    if (parameter == null) {
                        return;
                    }
                    Parameter definition = parameter.definition();
                    if (definition == null) {
                        return;
                    }
                    // don't repeat the inspection for other parameters
                    if (!parameter.holder().equals(expression)) {
                        return;
                    }
                    // we should check for duplicates only when defining list of fields
                    if (definition.parameterValueTypes() != null && definition.parameterValueTypes().stream().noneMatch(DQLDefinitionService.FIELD_IDENTIFIER_PARAMETER_VALUE_TYPES::contains)) {
                        return;
                    }
                    List<PsiElement> duplicatedFields = getDuplicatedFields(parameter);
                    for (PsiElement field : duplicatedFields) {
                        holder.registerProblem(
                                field,
                                DQLBundle.message("inspection.command.duplicatedFieldNames.duplicated", getFieldName(field)),
                                new DropElementQuickFix(),
                                new SetFieldNameQuickFix()
                        );
                    }
                }
            }
        };
    }

    private List<PsiElement> getDuplicatedFields(MappedParameter parameter) {
        Set<String> seenNames = new HashSet<>();
        List<PsiElement> invalidElements = new ArrayList<>();
        List<PsiElement> toProcess = new ArrayList<>();
        if (!parameter.included().isEmpty()) {
            toProcess.addAll(parameter.getExpressions());
        } else {
            toProcess.add(parameter.holder());
        }

        while (!toProcess.isEmpty()) {
            PsiElement expression = toProcess.removeFirst();
            if (expression instanceof DQLParameterExpression parameterExpression && parameterExpression.getExpression() != null) {
                expression = parameterExpression.getExpression();
            }
            if (expression instanceof DQLBracketExpression bracketExpression) {
                toProcess.addAll(bracketExpression.getExpressionList());
                continue;
            }

            String fieldName = getFieldName(expression);
            if (!seenNames.add(fieldName)) {
                invalidElements.add(expression);
            }
        }
        return invalidElements;
    }

    private @NotNull String getFieldName(@NotNull PsiElement element) {
        return switch (element) {
            case BaseElement e -> e.getFieldName();
            case PsiNamedElement named -> Objects.requireNonNullElse(named.getName(), "");
            default -> element.getText();
        };
    }
}
