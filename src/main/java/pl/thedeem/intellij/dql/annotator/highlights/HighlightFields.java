package pl.thedeem.intellij.dql.annotator.highlights;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.annotator.DQLAnnotatorEngine;
import pl.thedeem.intellij.dql.highlighting.DQLColorScheme;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import org.jetbrains.annotations.NotNull;

public class HighlightFields implements DQLAnnotatorEngine {
    @Override
    public @NotNull AnnotationResult annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof DQLFieldExpression field) {
            DQLAssignExpression assignExpression = field.getAssignExpression();
            holder.newSilentAnnotation(HighlightOptions.HIGHLIGHT_SEVERITY)
                    .range(element.getTextRange())
                    .textAttributes(assignExpression != null ? DQLColorScheme.DATA_ASSIGNED_FIELD : DQLColorScheme.DATA_FIELD)
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
