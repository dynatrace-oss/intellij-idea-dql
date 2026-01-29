package pl.thedeem.intellij.common.psi;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class PsiUtils {
    @Nullable
    public static <T> T getPrevSiblingOfTypeSkippingWhitespaces(PsiElement psiElement, Class<T> toFind) {
        if (psiElement == null) {
            return null;
        }
        for (PsiElement prevSibling = psiElement.getPrevSibling(); prevSibling != null; prevSibling = prevSibling.getPrevSibling()) {
            if (toFind.isInstance(prevSibling)) {
                return toFind.cast(prevSibling);
            }
            if (!PlatformPatterns.psiElement().whitespaceCommentEmptyOrError().accepts(prevSibling)) {
                return null;
            }
        }
        return null;
    }

    public static @NotNull List<PsiElement> getElementsUntilParent(PsiElement element, Class<?>... types) {
        return getElementsUntilParent(element, t -> true, types);
    }

    public static @NotNull List<PsiElement> getElementsUntilParent(PsiElement element, Predicate<PsiElement> allowed, Class<?>... types) {
        List<PsiElement> elements = new ArrayList<>();
        elements.add(element);
        PsiElement result = element.getParent();
        while (result != null) {
            elements.add(result);
            PsiElement finalResult = result;
            if (Arrays.stream(types).anyMatch(t -> t.isInstance(finalResult))) {
                break;
            }
            if (!allowed.test(result)) {
                return List.of();
            }
            result = result.getParent();
        }
        if (result == null) {
            return List.of();
        }
        return elements.reversed();
    }

    public static PsiElement getPreviousElement(PsiElement element) {
        PsiElement prev = element.getPrevSibling();
        while (prev != null && PlatformPatterns.psiElement().whitespaceCommentEmptyOrError().accepts(prev)) {
            prev = prev.getPrevSibling();
        }
        return prev;
    }

    public static PsiElement getDeepNeighbourElement(PsiElement element, ElementPattern<PsiElement> pattern) {
        PsiElement neighbour = PsiUtils.getPreviousElement(element);
        while (neighbour != null && !pattern.accepts(neighbour)) {
            neighbour = neighbour.getLastChild();
        }
        return neighbour;
    }

    public static PsiElement getNextElement(PsiElement element) {
        PsiElement next = element.getNextSibling();
        while (next != null && PlatformPatterns.psiElement().whitespaceCommentEmptyOrError().accepts(next)) {
            next = next.getNextSibling();
        }
        return next;
    }
}
