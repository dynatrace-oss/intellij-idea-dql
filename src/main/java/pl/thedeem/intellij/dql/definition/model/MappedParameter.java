package pl.thedeem.intellij.dql.definition.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.DQLBracketExpression;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

import java.util.*;

public record MappedParameter(
        @Nullable Parameter definition,
        @NotNull PsiElement holder,
        @NotNull List<PsiElement> included
) implements BaseTypedElement {
    public @Nullable String name() {
        if (definition != null) {
            return definition.name();
        }
        if (holder instanceof DQLParameterExpression named) {
            return named.getName();
        }
        return null;
    }

    public @NotNull List<List<PsiElement>> getParameterGroups() {
        List<List<PsiElement>> result = new ArrayList<>();

        PsiElement previous = null;
        for (PsiElement expression : getExpressions()) {
            DQLExpression prevSibling = PsiTreeUtil.getPrevSiblingOfType(expression, DQLExpression.class);
            if (prevSibling != previous || result.isEmpty()) {
                result.add(new ArrayList<>());
            }
            result.getLast().add(expression);
            previous = expression;
        }
        return result;
    }

    public boolean includes(PsiElement expression) {
        return holder == expression || included.contains(expression);
    }

    public boolean isExplicitlyNamed() {
        return holder instanceof DQLParameterExpression;
    }

    @Override
    public @NotNull Collection<String> getDataType() {
        if (included.isEmpty()) {
            if (holder instanceof BaseTypedElement entity) {
                return entity.getDataType();
            }
        } else {
            Set<String> result = new HashSet<>();
            for (PsiElement expression : getExpressions()) {
                if (expression instanceof BaseElement element) {
                    result.addAll(element.getDataType());
                }
            }
            return result;
        }
        return Set.of();
    }

    @Override
    public boolean accessesData() {
        for (PsiElement value : getExpressions()) {
            if (value instanceof BaseTypedElement entity && entity.accessesData()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance().calculateFieldName(holder);
    }

    public @NotNull List<PsiElement> getExpressions() {
        List<PsiElement> expressions = new ArrayList<>();
        expressions.add(holder);
        expressions.addAll(included);
        return expressions;
    }

    public @NotNull List<PsiElement> unpackExpressions() {
        List<PsiElement> result = new ArrayList<>();
        List<PsiElement> toProcess = new ArrayList<>(getExpressions());

        while (!toProcess.isEmpty()) {
            PsiElement element = DQLUtil.unpackParenthesis(toProcess.removeFirst());
            if (element instanceof DQLBracketExpression bracket) {
                toProcess.addAll(bracket.getExpressionList());
            } else if (element != null) {
                result.add(element);
            }
        }
        return result;
    }
}