package pl.thedeem.intellij.common.psi;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class PsiUtils {
    @Nullable
    public static <T> T getPrevSiblingOfTypeSkippingWhitespaces(@Nullable PsiElement psiElement, @NotNull Class<T> toFind) {
        return getPrevSiblingOfTypeSkippingWhitespaces(psiElement, toFind, Set.of());
    }

    public static <T> T getPrevSiblingOfTypeSkippingWhitespaces(
            @Nullable PsiElement psiElement,
            @NotNull Class<T> toFind,
            @NotNull Set<IElementType> skippable
    ) {
        if (psiElement == null) {
            return null;
        }
        for (PsiElement prev = psiElement.getPrevSibling(); prev != null; prev = prev.getPrevSibling()) {
            if (toFind.isInstance(prev)) {
                return toFind.cast(prev);
            }
            if (!skippable.contains(prev.getNode().getElementType()) &&
                    !PlatformPatterns.psiElement().whitespaceCommentEmptyOrError().accepts(prev)) {
                return null;
            }
        }
        return null;
    }

    @Nullable
    public static <T> T getNextSiblingOfTypeSkippingWhitespaces(
            @Nullable PsiElement psiElement,
            @NotNull Class<T> toFind,
            @NotNull Set<IElementType> skippable
    ) {
        if (psiElement == null) {
            return null;
        }
        for (PsiElement next = psiElement.getNextSibling(); next != null; next = next.getNextSibling()) {
            if (toFind.isInstance(next)) {
                return toFind.cast(next);
            }
            if (!skippable.contains(next.getNode().getElementType())
                    && !PlatformPatterns.psiElement().whitespaceCommentEmptyOrError().accepts(next)) {
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

    public static <T> @Nullable T getParentElement(PsiElement element, Predicate<PsiElement> allowed, Class<T> parentClass) {
        PsiElement result = element.getParent();
        while (result != null) {
            if (parentClass.isInstance(result)) {
                return parentClass.cast(result);
            }
            if (!allowed.test(result)) {
                return null;
            }
            result = result.getParent();
        }
        return null;
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
