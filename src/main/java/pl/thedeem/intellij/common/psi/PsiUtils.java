package pl.thedeem.intellij.common.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

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
        }
        return null;
    }

    @Nullable
    public static <T> T findDeepLastChildOfType(PsiElement psiElement, Class<T> toFind) {
        if (psiElement == null) {
            return null;
        }
        if (toFind.isInstance(psiElement)) {
            return toFind.cast(psiElement);
        }
        for (PsiElement lastChild = psiElement.getLastChild(); lastChild != null; lastChild = lastChild.getLastChild()) {
            if (toFind.isInstance(lastChild)) {
                return toFind.cast(lastChild);
            }
        }
        return null;
    }
}
