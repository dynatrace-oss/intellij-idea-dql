package pl.thedeem.intellij.dql.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.completion.CompletionUtils;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;

import java.util.Set;

public class DQLCompletionWeigher extends LookupElementWeigher {
    private static final Set<String> STATIC_SUGGESTION_TYPES = Set.of(
            AutocompleteUtils.STATIC,
            AutocompleteUtils.BOOLEAN,
            AutocompleteUtils.QUERY_START,
            AutocompleteUtils.FILE,
            AutocompleteUtils.SMARTSCAPE,
            AutocompleteUtils.METRIC,
            AutocompleteUtils.BUCKET
    );

    protected DQLCompletionWeigher() {
        super(DynatraceQueryLanguage.DQL_ID);
    }

    public @Nullable Comparable<?> weigh(@NotNull LookupElement element) {
        String userData = element.getUserData(CompletionUtils.LOOKUP_ELEMENT_KIND_KEY);
        if (userData != null) {
            if (STATIC_SUGGESTION_TYPES.contains(userData)) {
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