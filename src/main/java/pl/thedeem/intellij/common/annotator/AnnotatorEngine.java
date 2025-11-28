package pl.thedeem.intellij.common.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public interface AnnotatorEngine {
    @NotNull AnnotationResult annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder);
    @NotNull AnnotationType getType();

    enum AnnotationResult {
        PASS,
        STOP_FOR_SAME_TYPE,
        FORCE_STOP,
    }

    enum AnnotationType {
        HIGHLIGHT,
        ISSUE
    }
}
