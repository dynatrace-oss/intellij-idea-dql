package pl.thedeem.intellij.common;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;

import java.util.Set;

public class LangUtils {
    public static boolean shouldAddSeparatorBefore(InsertionContext context, Set<Character> validCharacters) {
        Character character = LangUtils.getPreviousNonEmptyCharacterFromDocument(context);
        return character != null && !validCharacters.contains(character);
    }

    public static Character getPreviousNonEmptyCharacterFromDocument(InsertionContext context) {
        int i = context.getStartOffset();
        String result;
        do {
            result = getStringFromDocument(context, i - 1, i);
            i--;
        } while (i >= 0 && result.isBlank() && !result.isEmpty());
        return !result.isEmpty() ? result.charAt(0) : null;
    }

    public static String getStringFromDocument(InsertionContext context, int startOffset, int endOffset) {
        return context.getDocument().getText(new TextRange(
                Math.max(0, startOffset),
                Math.min(context.getDocument().getTextLength() - 1, endOffset)
        ));
    }

    public static PsiElement getPreviousElement(PsiElement element) {
        PsiElement prev = element.getPrevSibling();
        while (prev != null && PlatformPatterns.psiElement().whitespaceCommentEmptyOrError().accepts(prev)) {
            prev = prev.getPrevSibling();
        }
        return prev;
    }

    public static PsiElement getNextElement(PsiElement element) {
        PsiElement next = element.getNextSibling();
        while (next != null && PlatformPatterns.psiElement().whitespaceCommentEmptyOrError().accepts(next)) {
            next = next.getNextSibling();
        }
        return next;
    }
}
