package pl.thedeem.intellij.dql.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DQLCompletionWeigher extends LookupElementWeigher {

    protected DQLCompletionWeigher() {
        super(DynatraceQueryLanguage.DQL_ID);
    }

    public @Nullable Comparable<?> weigh(@NotNull LookupElement element) {
        String userData = element.getUserData(AutocompleteUtils.LOOKUP_ELEMENT_KIND_KEY);
        if (userData != null) {
            if (List.of(AutocompleteUtils.STATIC, AutocompleteUtils.BOOLEAN, AutocompleteUtils.QUERY_START).contains(userData)) {
                return 0;
            }
            if (AutocompleteUtils.COMMAND_PARAMETER.equals(userData)) {
                return 5;
            }
            if (AutocompleteUtils.FUNCTION.equals(userData)) {
                return 20;
            }
            if (AutocompleteUtils.COMMAND.equals(userData)) {
                return 50;
            }
        }
        return 100;
    }
}