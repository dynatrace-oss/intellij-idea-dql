package pl.thedeem.intellij.common.psi;

import com.intellij.patterns.PlatformPatterns;
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
            } else if (!PlatformPatterns.psiElement().whitespaceCommentEmptyOrError().accepts(prevSibling)) {
                return null;
            }
        }
        return null;
    }
}
