package pl.thedeem.intellij.dql.annotator.highlights;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.annotator.DQLAnnotatorEngine;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.highlighting.DQLColorScheme;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import org.jetbrains.annotations.NotNull;

public class HighlightEnumValues implements DQLAnnotatorEngine {
    @Override
    public @NotNull AnnotationResult annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof DQLFieldExpression fieldExpression
                && fieldExpression.getParent() instanceof DQLParameterExpression argument
                && argument.getParent() instanceof DQLQueryStatement statement
        ) {
            DQLParameterObject parameter = statement.getParameter(argument);
            if (parameter != null && parameter.getDefinition() != null && parameter.getDefinition().enumValues != null && !parameter.getDefinition().enumValues.isEmpty()) {
                holder.newSilentAnnotation(HighlightOptions.HIGHLIGHT_SEVERITY)
                        .range(element.getTextRange())
                        .textAttributes(DQLColorScheme.ENUM_VALUE)
                        .create();
                return AnnotationResult.STOP_FOR_SAME_TYPE;
            }
        }
        return AnnotationResult.PASS;
    }

    @Override
    public @NotNull AnnotationType getType() {
        return AnnotationType.HIGHLIGHT;
    }
}
