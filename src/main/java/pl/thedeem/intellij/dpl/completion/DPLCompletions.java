package pl.thedeem.intellij.dpl.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.completion.CompletionUtils;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.completion.insertions.DPLCommandInsertionHandler;
import pl.thedeem.intellij.dpl.completion.insertions.DPLConfigurationInsertionHandler;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;

public class DPLCompletions {
    public static final String CONFIGURATION_PARAMETER = DPLBundle.message("completion.configurationParameter");
    public static final String COMMAND = DPLBundle.message("completion.command");
    public static final String VARIABLE = DPLBundle.message("completion.variable");

    @NotNull
    public static LookupElementBuilder createConfigurationParameterLookup(@NotNull Configuration parameter) {
        return CompletionUtils.createLookupElement(
                parameter.name(),
                DPLIcon.CONFIGURATION_PARAMETER,
                CONFIGURATION_PARAMETER,
                parameter.suggestion(),
                new DPLConfigurationInsertionHandler(parameter)
        );
    }

    @NotNull
    public static LookupElementBuilder createConfigurationParameterLookup(@NotNull ExpressionDescription description) {
        return CompletionUtils.createLookupElement(
                description.name().toUpperCase(),
                DPLIcon.COMMAND,
                COMMAND,
                null,
                new DPLCommandInsertionHandler(description)
        );
    }
}
