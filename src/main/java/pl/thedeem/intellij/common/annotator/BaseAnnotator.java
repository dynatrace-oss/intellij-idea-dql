package pl.thedeem.intellij.common.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class BaseAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        Map<AnnotatorEngine.AnnotationType, AnnotatorEngine.AnnotationResult> results = new HashMap<>();
        for (AnnotatorEngine engine : getEngines()) {
            AnnotatorEngine.AnnotationResult groupStatus = results.getOrDefault(engine.getType(), AnnotatorEngine.AnnotationResult.PASS);
            if (groupStatus == AnnotatorEngine.AnnotationResult.PASS) {
                AnnotatorEngine.AnnotationResult result = engine.annotate(element, holder);
                results.put(engine.getType(), result);
                if (result == AnnotatorEngine.AnnotationResult.FORCE_STOP) {
                    break;
                }
            }
        }
    }

    public abstract List<AnnotatorEngine> getEngines();
}
