package pl.thedeem.intellij.dpl.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.completion.CompletionUtils;
import pl.thedeem.intellij.dpl.DynatracePatternLanguage;

public class DPLCompletionWeigher extends LookupElementWeigher {

    protected DPLCompletionWeigher() {
        super(DynatracePatternLanguage.DPL_ID);
    }

    public @Nullable Comparable<?> weigh(@NotNull LookupElement element) {
        String userData = element.getUserData(CompletionUtils.LOOKUP_ELEMENT_KIND_KEY);
        if (userData != null) {
            if (DPLCompletions.CONFIGURATION_PARAMETER.equals(userData)) {
                return 0;
            }
            if (DPLCompletions.VARIABLE.equals(userData)) {
                return 5;
            }
            if (DPLCompletions.COMMAND.equals(userData)) {
                return 10;
            }
        }
        return 100;
    }
}