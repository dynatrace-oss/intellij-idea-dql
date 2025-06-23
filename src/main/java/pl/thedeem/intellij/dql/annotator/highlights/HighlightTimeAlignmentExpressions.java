package pl.thedeem.intellij.dql.annotator.highlights;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.annotator.DQLAnnotatorEngine;
import pl.thedeem.intellij.dql.highlighting.DQLColorScheme;
import pl.thedeem.intellij.dql.psi.elements.TimeAlignmentExpression;

import java.util.Objects;

public class HighlightTimeAlignmentExpressions implements DQLAnnotatorEngine {
    @Override
    public @NotNull AnnotationResult annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof TimeAlignmentExpression timeAlignmentExpression) {
            PsiElement target = Objects.requireNonNullElse(timeAlignmentExpression.getDurationElement(), timeAlignmentExpression);
            holder.newSilentAnnotation(HighlightOptions.HIGHLIGHT_SEVERITY)
                    .range(target.getTextRange())
                    .textAttributes(DQLColorScheme.DURATION)
                    .create();
            return AnnotationResult.STOP_FOR_SAME_TYPE;
        }
        return AnnotationResult.PASS;
    }

    @Override
    public @NotNull AnnotationType getType() {
        return AnnotationType.HIGHLIGHT;
    }
}
