package pl.thedeem.intellij.dql.inspections.commands;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.inspections.fixes.DropElementQuickFix;
import pl.thedeem.intellij.dql.inspections.fixes.SetFieldNameQuickFix;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicatedFieldNamesInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);
                if (expression.getParent() instanceof DQLQueryStatement command) {
                    DQLParameterObject parameter = command.getParameter(expression);
                    if (parameter == null) {
                        return;
                    }
                    DQLParameterDefinition definition = parameter.getDefinition();
                    if (definition == null || definition.allowsDuplicates()) {
                        return;
                    }
                    List<PsiElement> duplicatedFields = getDuplicatedFields(parameter);
                    for (PsiElement field : duplicatedFields) {
                        holder.registerProblem(
                            field,
                            DQLBundle.message("inspection.command.duplicatedFieldNames.duplicated"),
                            new DropElementQuickFix(),
                            new SetFieldNameQuickFix()
                        );
                    }
                }
            }
        };
    }

    private List<PsiElement> getDuplicatedFields(DQLParameterObject parameter) {
        Set<String> seenNames = new HashSet<>();
        List<PsiElement> invalidElements = new ArrayList<>();
        List<PsiElement> toProcess = new ArrayList<>();
        if (parameter.getValues().size() > 1 && parameter.getExpression() == parameter.getValues().getFirst()) {
            toProcess.addAll(parameter.getExpressions());
        } else {
            toProcess.add(parameter.getExpression());
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

            String fieldName = switch (expression) {
                case BaseElement e -> e.getFieldName();
                case PsiNamedElement named -> named.getName();
                default -> expression.getText();
            };
            if (!seenNames.add(fieldName)) {
                invalidElements.add(expression);
            }
        }
        return invalidElements;
    }
}
