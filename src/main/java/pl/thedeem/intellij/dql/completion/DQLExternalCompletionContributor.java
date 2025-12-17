package pl.thedeem.intellij.dql.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.engines.DQLDynatraceAutocomplete;
import pl.thedeem.intellij.dql.settings.DQLSettings;

public class DQLExternalCompletionContributor extends CompletionContributor {

    public DQLExternalCompletionContributor() {
        extend(CompletionType.BASIC, DQLPsiPatterns.SUGGEST_FIELD_NAMES,
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                        PsiFile file = parameters.getOriginalFile();
                        if (DQLUtil.isPartialFile(file) || !DQLSettings.getInstance().isUseDynatraceAutocompleteEnabled()) {
                            return;
                        }
                        new DQLDynatraceAutocomplete().autocomplete(parameters, resultSet);
                    }
                }
        );
    }
}
