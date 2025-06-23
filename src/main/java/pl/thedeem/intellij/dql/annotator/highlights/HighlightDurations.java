package pl.thedeem.intellij.dql.annotator.highlights;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.annotator.DQLAnnotatorEngine;
import pl.thedeem.intellij.dql.highlighting.DQLColorScheme;
import pl.thedeem.intellij.dql.psi.DQLDuration;
import org.jetbrains.annotations.NotNull;

public class HighlightDurations implements DQLAnnotatorEngine {
    @Override
    public @NotNull AnnotationResult annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof DQLDuration) {
            holder.newSilentAnnotation(HighlightOptions.HIGHLIGHT_SEVERITY)
                    .range(element.getTextRange())
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
