package pl.thedeem.intellij.dql.annotator.highlights;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.annotator.AnnotatorEngine;
import pl.thedeem.intellij.common.annotator.HighlightOptions;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.highlighting.DQLColorScheme;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;

public class HighlightEnumValues implements AnnotatorEngine {
    @Override
    public @NotNull AnnotationResult annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof DQLFieldExpression fieldExpression
                && fieldExpression.getParent() instanceof DQLParameterExpression argument
                && argument.getParent() instanceof DQLQueryStatement statement
        ) {
            MappedParameter parameter = statement.getParameter(argument);
            Parameter definition = parameter != null ? parameter.definition() : null;
            if (definition != null && definition.allowedEnumValues() != null && !definition.allowedEnumValues().isEmpty()) {
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
