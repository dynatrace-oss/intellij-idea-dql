package pl.thedeem.intellij.dql.annotator.highlights;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.common.annotator.AnnotatorEngine;
import pl.thedeem.intellij.common.annotator.HighlightOptions;
import pl.thedeem.intellij.dql.highlighting.DQLColorScheme;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import org.jetbrains.annotations.NotNull;

public class HighlightFunctions implements AnnotatorEngine {
    @Override
    public @NotNull AnnotationResult annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof DQLFunctionCallExpression callExpression) {
            holder.newSilentAnnotation(HighlightOptions.HIGHLIGHT_SEVERITY)
                .range(callExpression.getFunctionName().getTextRange())
                .textAttributes(DQLColorScheme.FUNCTION)
                .create();

            for (DQLExpression expression : callExpression.getExpressionList()) {
                if (expression instanceof DQLParameterExpression parameterExpression) {
                    holder.newSilentAnnotation(HighlightOptions.HIGHLIGHT_SEVERITY)
                        .range(parameterExpression.getParameterName().getTextRange())
                        .textAttributes(DQLColorScheme.FUNCTION_PARAMETER)
                        .create();
                }
            }

            return AnnotationResult.STOP_FOR_SAME_TYPE;
        }
        return AnnotationResult.PASS;
    }

    @Override
    public @NotNull AnnotationType getType() {
        return AnnotationType.HIGHLIGHT;
    }
}
