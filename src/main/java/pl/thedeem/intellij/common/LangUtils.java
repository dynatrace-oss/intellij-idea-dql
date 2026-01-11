package pl.thedeem.intellij.common;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.openapi.util.TextRange;

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
}
