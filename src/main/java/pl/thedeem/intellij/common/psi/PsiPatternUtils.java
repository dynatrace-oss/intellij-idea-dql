package pl.thedeem.intellij.common.psi;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class PsiPatternUtils {
    public static PatternCondition<PsiElement> isLastChildUntil(ElementPattern<? extends PsiElement> expectedParent) {
        return new PatternCondition<>("isLastChildUntil") {
            @Override
            public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                PsiElement processed = element;
                do {
                    if (processed.getNextSibling() != null && !TokenType.ERROR_ELEMENT.equals(processed.getNextSibling().getNode().getElementType())) {
                        return false;
                    }
                    processed = processed.getParent();
                }
                while (processed != null && !expectedParent.accepts(processed, context));
                return processed != null;
            }
        };
    }

    public static PatternCondition<PsiElement> isAfterElementSkipping(ElementPattern<? extends PsiElement> expected, ElementPattern<? extends PsiElement> skipping) {
        return new PatternCondition<>("isAfterElementSkipping") {
            @Override
            public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                PsiElement processed = element;
                do {
                    processed = processed.getPrevSibling();
                } while (processed != null && skipping.accepts(processed, context));
                return processed != null && expected.accepts(processed, context);
            }
        };
    }

    public static PatternCondition<PsiElement> withNumberOfChildren(int number, ElementPattern<? extends PsiElement> skipping) {
        return new PatternCondition<>("withNumberOfChildren") {
            @Override
            public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                PsiElement [] processed = element.getChildren();
                int result = 0;
                for (PsiElement child : processed) {
                    if (skipping != null && !skipping.accepts(child, context)) {
                        result++;
                    }
                }
                return result == number;
            }
        };
    }

    public static PatternCondition<PsiElement> isDeepNeighbourOf(ElementPattern<? extends PsiElement> expected) {
        return new PatternCondition<>("isDeepNeighbourOf") {
            @Override
            public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                if (expected.accepts(element, context)) {
                    return true;
                }
                PsiElement processed = element;
                do {
                    processed = processed.getLastChild();
                } while (processed != null && !expected.accepts(processed, context));
                return processed != null;
            }
        };
    }
}
