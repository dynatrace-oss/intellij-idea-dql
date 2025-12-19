package pl.thedeem.intellij.dql.completion;

import com.intellij.codeInsight.completion.CompletionResultSet;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.completion.CompletionUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.insertions.*;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.definition.model.Signature;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import java.util.stream.Collectors;

public class AutocompleteUtils {
    public static final String QUERY_START = DQLBundle.message("autocomplete.types.queryStart");
    public static final String COMMAND = DQLBundle.message("autocomplete.types.command");
    public static final String COMMAND_PARAMETER = DQLBundle.message("autocomplete.types.commandParameter");
    public static final String FUNCTION = DQLBundle.message("autocomplete.types.function");
    public static final String BOOLEAN = DQLBundle.message("autocomplete.types.boolean");
    public static final String STATIC = DQLBundle.message("autocomplete.types.static");
    public static final String EXPRESSION = DQLBundle.message("autocomplete.types.expression");
    public static final String DATA_REFERENCE = DQLBundle.message("autocomplete.types.definedField");
    public static final String VARIABLE = DQLBundle.message("autocomplete.types.variable");

    public static void autocompleteBooleans(@NotNull CompletionResultSet result) {
        result.addElement(CompletionUtils.createLookupElement(
                "not",
                DQLIcon.DQL_OPERAND,
                AutocompleteUtils.STATIC,
                null,
                new DQLValueAfterOperandInsertionHandler("not")
        ));
        for (String booleanValue : new String[]{"true", "false"}) {
            result.addElement(CompletionUtils.createLookupElement(
                    booleanValue,
                    DQLIcon.DQL_BOOLEAN,
                    AutocompleteUtils.BOOLEAN,
                    null,
                    null
            ));
        }
    }

    public static void autocompleteStaticValue(@NotNull String enumValue, @NotNull CompletionResultSet result) {
        result.addElement(CompletionUtils.createLookupElement(
                enumValue,
                DQLIcon.DQL_BOOLEAN,
                AutocompleteUtils.STATIC,
                null,
                null
        ));
    }

    public static void autocompleteSortingKeyword(@NotNull CompletionResultSet result) {
        for (String operand : new String[]{"asc", "desc"}) {
            result.addElement(CompletionUtils.createLookupElement(
                    operand,
                    DQLIcon.DQL_SORT_DIRECTION,
                    AutocompleteUtils.STATIC,
                    null,
                    null
            ));
        }
    }

    public static void autocompleteCurrentTimestamp(@NotNull CompletionResultSet result) {
        String currentTimestamp = "\"" + DQLUtil.getCurrentTimeTimestamp() + "\"";

        result.addElement(CompletionUtils.createLookupElement(
                        currentTimestamp,
                        DQLIcon.DQL_STATEMENT_PARAMETER,
                        AutocompleteUtils.STATIC,
                        " = " + currentTimestamp,
                        null
                )
                .withPresentableText(DQLBundle.message("autocomplete.list.currentTimestamp"))
                .withBoldness(true));
    }

    public static void autocomplete(@NotNull Command command, @NotNull CompletionResultSet result) {
        if (command.experimental() && !DQLSettings.getInstance().isAllowingExperimentalFeatures()) {
            return;
        }
        result.addElement(
                CompletionUtils.createLookupElement(
                        command.name(),
                        DQLIcon.DQL_QUERY_COMMAND,
                        AutocompleteUtils.COMMAND,
                        " " + String.join(", ", command.requiredParameters().stream()
                                .map(p -> p.name() + ": ...")
                                .collect(Collectors.toSet())),
                        new DQLStatementInsertionHandler(command)
                ).withItemTextItalic(command.experimental())
        );
    }

    public static void autocomplete(@NotNull Parameter parameter, @NotNull CompletionResultSet result, boolean addComma) {
        if (parameter.hidden()) {
            return;
        }
        if (parameter.experimental() && !DQLSettings.getInstance().isAllowingExperimentalFeatures()) {
            return;
        }
        result.addElement(
                CompletionUtils.createLookupElement(
                                parameter.name(),
                                DQLIcon.DQL_STATEMENT_PARAMETER,
                                AutocompleteUtils.COMMAND_PARAMETER,
                                parameter.requiresName() ? ": ..." : "",
                                new DQLNamedParameterInsertionHandler(parameter, addComma))
                        .withBoldness(true)
                        .withItemTextItalic(parameter.experimental())
        );
    }

    public static void autocomplete(@NotNull Function function, @NotNull CompletionResultSet result) {
        if (function.experimental() && !DQLSettings.getInstance().isAllowingExperimentalFeatures()) {
            return;
        }
        if (function.signatures() != null) {
            for (Signature signature : function.signatures()) {
                if (!signature.experimental() || DQLSettings.getInstance().isAllowingExperimentalFeatures()) {
                    result.addElement(
                            CompletionUtils.createLookupElement(
                                            function.name(),
                                            DQLIcon.DQL_FUNCTION,
                                            AutocompleteUtils.FUNCTION,
                                            "(" + String.join(", ", signature.requiredParameters().stream().map(Parameter::name).collect(Collectors.toSet())) + ")",
                                            new DQLFunctionInsertionHandler(function, signature)
                                    )
                                    .withStrikeoutness(function.deprecated())
                                    .withItemTextItalic(function.experimental() || signature.experimental())
                    );
                }
            }
        }
    }

    public static void autocompleteConditionOperands(@NotNull CompletionResultSet result) {
        for (String operand : new String[]{"and", "or", "xor"}) {
            result.addElement(CompletionUtils.createLookupElement(
                    operand,
                    DQLIcon.DQL_OPERAND,
                    AutocompleteUtils.STATIC,
                    null,
                    new DQLValueAfterOperandInsertionHandler(operand)
            ));
        }
    }

    public static void autocompleteInExpression(@NotNull CompletionResultSet result) {
        result.addElement(CompletionUtils.createLookupElement(
                "in",
                DQLIcon.DQL_OPERAND,
                AutocompleteUtils.EXPRESSION,
                "[ ]",
                new DQLInExpressionInsertionHandler()
        ));
    }

    public static void autocompleteStringValues(@NotNull CompletionResultSet result) {
        result.addElement(CompletionUtils.createLookupElement(
                "\"...\"",
                DQLIcon.DQL_STRING,
                AutocompleteUtils.STATIC,
                "",
                new DQLStringInsertionHandler("\"")
        ));
        result.addElement(CompletionUtils.createLookupElement(
                "\"\"\"...\"\"\"",
                DQLIcon.DQL_STRING,
                AutocompleteUtils.STATIC,
                "",
                new DQLStringInsertionHandler("\"\"\"")
        ));
    }
}
