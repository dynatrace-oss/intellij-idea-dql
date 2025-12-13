package pl.thedeem.intellij.dql.definition.model;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;

import java.util.*;

public record MappedParameter(
        @Nullable Parameter definition,
        @NotNull DQLExpression holder,
        @NotNull List<DQLExpression> included
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

    public @NotNull TextRange getTextRange() {
        PsiElement lastElement = included.isEmpty() ? holder : included.getLast();
        return new TextRange(holder.getTextRange().getStartOffset(), lastElement.getTextRange().getEndOffset());
    }

    public boolean includes(DQLExpression expression) {
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
            for (DQLExpression expression : getExpressions()) {
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
        for (DQLExpression value : getExpressions()) {
            if (value instanceof BaseTypedElement entity && entity.accessesData()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator().addPart(holder).getFieldName();
    }

    public @NotNull Collection<DQLExpression> getExpressions() {
        List<DQLExpression> expressions = new ArrayList<>();
        expressions.add(holder);
        expressions.addAll(included);
        return expressions;
    }
}