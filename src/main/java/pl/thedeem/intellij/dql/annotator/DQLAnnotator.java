package pl.thedeem.intellij.dql.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.annotator.highlights.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


final class DQLAnnotator implements Annotator {
    public static final List<DQLAnnotatorEngine> ENGINES = List.of(
            new HighlightStatementKeywords(),
            new HighlightSortDirection(),
            new HighlightFunctions(),
            new HighlightEnumValues(),
            new HighlightFields(),
            new HighlightOperatorKeywords(),
            new HighlightDurations(),
            new HighlightTimeAlignmentExpressions()
    );

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        Map<DQLAnnotatorEngine.AnnotationType, DQLAnnotatorEngine.AnnotationResult> results = new HashMap<>();
        for (DQLAnnotatorEngine engine : ENGINES) {
            DQLAnnotatorEngine.AnnotationResult groupStatus = results.getOrDefault(engine.getType(), DQLAnnotatorEngine.AnnotationResult.PASS);
            if (groupStatus == DQLAnnotatorEngine.AnnotationResult.PASS) {
                DQLAnnotatorEngine.AnnotationResult result = engine.annotate(element, holder);
                results.put(engine.getType(), result);
                if (result == DQLAnnotatorEngine.AnnotationResult.FORCE_STOP) {
                    break;
                }
            }
        }
    }
}
