package pl.thedeem.intellij.dql.annotator.highlights;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.annotator.AnnotatorEngine;
import pl.thedeem.intellij.common.annotator.HighlightOptions;
import pl.thedeem.intellij.dql.highlighting.DQLColorScheme;
import pl.thedeem.intellij.dql.psi.DQLCommandKeyword;

public class HighlightStatementKeywords implements AnnotatorEngine {
    @Override
    public @NotNull AnnotationResult annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof DQLCommandKeyword) {
            holder.newSilentAnnotation(HighlightOptions.HIGHLIGHT_SEVERITY)
                    .range(element.getTextRange())
                    .textAttributes(DQLColorScheme.STATEMENT_KEYWORD)
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
