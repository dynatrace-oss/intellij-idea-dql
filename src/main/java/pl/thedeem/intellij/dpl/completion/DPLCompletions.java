package pl.thedeem.intellij.dpl.completion;

import com.intellij.codeInsight.completion.CompletionResultSet;
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

    public static void createConfigurationParameterLookup(@NotNull Configuration parameter, @NotNull CompletionResultSet result) {
        result.addElement(CompletionUtils.createLookupElement(
                parameter.name(),
                DPLIcon.CONFIGURATION_PARAMETER,
                CONFIGURATION_PARAMETER,
                parameter.suggestion(),
                new DPLConfigurationInsertionHandler(parameter)
        ));
    }

    public static void createConfigurationParameterLookup(@NotNull ExpressionDescription description, @NotNull CompletionResultSet result) {
        result.addElement(CompletionUtils.createLookupElement(
                description.name().toUpperCase(),
                DPLIcon.COMMAND,
                COMMAND,
                null,
                new DPLCommandInsertionHandler(description.name())
        ));
        if (description.aliases() != null) {
            for (String alias : description.aliases()) {
                result.addElement(CompletionUtils.createLookupElement(
                        alias.toUpperCase(),
                        DPLIcon.COMMAND,
                        COMMAND,
                        null,
                        new DPLCommandInsertionHandler(alias)
                ));
            }
        }
    }
}
