package pl.thedeem.intellij.dql.completion;

import com.intellij.codeInsight.completion.CompletionResultSet;
import pl.thedeem.intellij.common.completion.CompletionUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.insertions.*;
import pl.thedeem.intellij.dql.definition.DQLCommandDefinition;
import pl.thedeem.intellij.dql.definition.DQLFunctionDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;

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

    public static void autocompleteFunction(DQLFunctionDefinition function, CompletionResultSet result) {
        if (function != null) {
            result.addElement(CompletionUtils.createLookupElement(
                    function.name,
                    DQLIcon.DQL_FUNCTION,
                    AutocompleteUtils.FUNCTION,
                    "(" + String.join(", ", function.getRequiredParameters().stream().map(p -> p.name).collect(Collectors.toSet())) + ")",
                    new DQLFunctionInsertionHandler(function)
            ));
        }
    }

    public static void autocompleteBooleans(CompletionResultSet result) {
        result.addElement(CompletionUtils.createLookupElement(
                "not",
                DQLIcon.DQL_FUNCTION,
                AutocompleteUtils.STATIC,
                null,
                new DQLValueAfterOperandInsertionHandler("not")
        ));
        for (String booleanValue : new String[]{"true", "false"}) {
            result.addElement(CompletionUtils.createLookupElement(
                    booleanValue,
                    DQLIcon.DQL_FUNCTION,
                    AutocompleteUtils.BOOLEAN,
                    null,
                    null
            ));
        }
    }

    public static void autocompleteStaticValue(String enumValue, CompletionResultSet result) {
        result.addElement(CompletionUtils.createLookupElement(
                enumValue,
                DQLIcon.DQL_FUNCTION,
                AutocompleteUtils.STATIC,
                null,
                null
        ));
    }

    public static void autocompleteSortingKeyword(CompletionResultSet result) {
        for (String operand : new String[]{"asc", "desc"}) {
            result.addElement(CompletionUtils.createLookupElement(
                    operand,
                    DQLIcon.DQL_FUNCTION,
                    AutocompleteUtils.STATIC,
                    null,
                    null
            ));
        }
    }

    public static void autocompleteParameter(DQLParameterDefinition parameter, CompletionResultSet result, boolean addComma) {
        if (parameter != null) {
            result.addElement(
                    CompletionUtils.createLookupElement(
                                    parameter.name,
                                    DQLIcon.DQL_STATEMENT_PARAMETER,
                                    AutocompleteUtils.COMMAND_PARAMETER,
                                    ": ...",
                                    new DQLNamedParameterInsertionHandler(parameter.name, parameter.getDQLTypes(), addComma, parameter.defaultValue)
                            )
                            .withBoldness(true)
            );
        }
    }

    public static void autocompleteCurrentTimestamp(CompletionResultSet result) {
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

    public static void autocompleteStatement(DQLCommandDefinition command, String type, CompletionResultSet result) {
        result.addElement(CompletionUtils.createLookupElement(
                command.name,
                DQLIcon.DQL_QUERY_COMMAND,
                type,
                " " + String.join(", ", command.getRequiredParameters().stream().map(p -> p.name + ": ...").collect(Collectors.toSet())),
                new DQLStatementInsertionHandler(command))
        );
    }

    public static void autocompleteConditionOperands(CompletionResultSet result) {
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

    public static void autocompleteInExpression(CompletionResultSet result) {
        result.addElement(CompletionUtils.createLookupElement(
                "in",
                DQLIcon.DQL_OPERAND,
                AutocompleteUtils.EXPRESSION,
                "[ ]",
                new DQLInExpressionInsertionHandler()
        ));
    }
}
