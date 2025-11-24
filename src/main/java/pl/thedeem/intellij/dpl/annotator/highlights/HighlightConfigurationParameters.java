package pl.thedeem.intellij.dpl.annotator.highlights;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.highlighting.DPLColorScheme;
import pl.thedeem.intellij.dpl.psi.DPLParameterName;
import pl.thedeem.intellij.common.annotator.AnnotatorEngine;
import pl.thedeem.intellij.common.annotator.HighlightOptions;

public class HighlightConfigurationParameters implements AnnotatorEngine {
    @Override
    public @NotNull AnnotationResult annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof DPLParameterName parameter) {
            holder.newSilentAnnotation(HighlightOptions.HIGHLIGHT_SEVERITY)
                    .range(parameter.getTextRange())
                    .textAttributes(DPLColorScheme.CONFIGURATION_NAME)
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
