package pl.thedeem.intellij.common.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Key;

import javax.swing.*;

public class CompletionUtils {
    public static final Key<String> LOOKUP_ELEMENT_KIND_KEY = Key.create("LOOKUP_ELEMENT_KIND");

    public static LookupElementBuilder createLookupElement(String name, Icon icon, String type, String description, InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder element = LookupElementBuilder.create(name)
                .withCaseSensitivity(false)
                .withTypeText(type)
                .withIcon(icon);
        if (description != null) {
            element = element.withTailText(description, true);
        }
        if (insertHandler != null) {
            element = element.withInsertHandler(insertHandler);
        }
        element.putUserData(LOOKUP_ELEMENT_KIND_KEY, type);
        return element;
    }
}
