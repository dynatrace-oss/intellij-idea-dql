package pl.thedeem.intellij.dql.completion;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Key;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.util.ProcessingContext;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.insertions.*;
import pl.thedeem.intellij.dql.definition.DQLCommandDefinition;
import pl.thedeem.intellij.dql.definition.DQLFunctionDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.stream.Collectors;

public class AutocompleteUtils {
    public static final Key<String> LOOKUP_ELEMENT_KIND_KEY = Key.create("LOOKUP_ELEMENT_KIND");
    public static final String QUERY_START = DQLBundle.message("autocomplete.types.queryStart");
    public static final String COMMAND = DQLBundle.message("autocomplete.types.command");
    public static final String COMMAND_PARAMETER = DQLBundle.message("autocomplete.types.commandParameter");
    public static final String FUNCTION = DQLBundle.message("autocomplete.types.function");
    public static final String BOOLEAN = DQLBundle.message("autocomplete.types.boolean");
    public static final String STATIC = DQLBundle.message("autocomplete.types.static");
    public static final String EXPRESSION = DQLBundle.message("autocomplete.types.expression");
    public static final String DATA_REFERENCE = DQLBundle.message("autocomplete.types.definedField");
    public static final String VARIABLE = DQLBundle.message("autocomplete.types.variable");

    public static LookupElementBuilder createLookupElement(String name, Icon icon, String type, String description, InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder element = LookupElementBuilder.create(name)
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

    public static PatternCondition<PsiElement> isLastChildUntil(ElementPattern<? extends PsiElement> expectedParent) {
        return new PatternCondition<>("isLastChildUntil") {
            @Override
            public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                PsiElement processed = element;
                do {
                    if (processed.getNextSibling() != null && !TokenType.ERROR_ELEMENT.equals(processed.getNextSibling().getNode().getElementType())) {
                        return false;
                    }
                    processed = processed.getParent();
                }
                while (processed != null && !expectedParent.accepts(processed, context));
                return processed != null;
            }
        };
    }

    public static PatternCondition<PsiElement> isAfterElementSkipping(ElementPattern<? extends PsiElement> expected, ElementPattern<? extends PsiElement> skipping) {
        return new PatternCondition<>("isAfterElementSkipping") {
            @Override
            public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                PsiElement processed = element;
                do {
                    processed = processed.getPrevSibling();
                } while (processed != null && skipping.accepts(processed, context));
                return processed != null && expected.accepts(processed, context);
            }
        };
    }

    public static PatternCondition<PsiElement> isDeepNeighbourOf(ElementPattern<? extends PsiElement> expected) {
        return new PatternCondition<>("isDeepNeighbourOf") {
            @Override
            public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                if (expected.accepts(element, context)) {
                    return true;
                }
                PsiElement processed = element;
                do {
                    processed = processed.getLastChild();
                } while (processed != null && !expected.accepts(processed, context));
                return processed != null;
            }
        };
    }

    public static void autocompleteFunction(DQLFunctionDefinition function, CompletionResultSet result) {
        if (function != null) {
            result.addElement(AutocompleteUtils.createLookupElement(
                    function.name,
                    DQLIcon.DQL_FUNCTION,
                    AutocompleteUtils.FUNCTION,
                    "(" + String.join(", ", function.getRequiredParameters().stream().map(p -> p.name).collect(Collectors.toSet())) + ")",
                    new DQLFunctionInsertionHandler(function)
            ));
        }
    }

    public static void autocompleteBooleans(CompletionResultSet result) {
        result.addElement(AutocompleteUtils.createLookupElement(
                "not",
                DQLIcon.DQL_FUNCTION,
                AutocompleteUtils.STATIC,
                null,
                new DQLValueAfterOperandInsertionHandler("not")
        ));
        for (String booleanValue : new String[]{"true", "false"}) {
            result.addElement(AutocompleteUtils.createLookupElement(
                    booleanValue,
                    DQLIcon.DQL_FUNCTION,
                    AutocompleteUtils.BOOLEAN,
                    null,
                    null
            ));
        }
    }

    public static void autocompleteStaticValue(String enumValue, CompletionResultSet result) {
        result.addElement(AutocompleteUtils.createLookupElement(
                enumValue,
                DQLIcon.DQL_FUNCTION,
                AutocompleteUtils.STATIC,
                null,
                null
        ));
    }

    public static void autocompleteSortingKeyword(CompletionResultSet result) {
        for (String operand : new String[]{"asc", "desc"}) {
            result.addElement(AutocompleteUtils.createLookupElement(
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
                    AutocompleteUtils.createLookupElement(
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
        String currentTimestamp = DQLUtil.getCurrentTimeTimestamp();

        result.addElement(AutocompleteUtils.createLookupElement(
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
        result.addElement(AutocompleteUtils.createLookupElement(
                command.name,
                DQLIcon.DQL_QUERY_COMMAND,
                type,
                " " + String.join(", ", command.getRequiredParameters().stream().map(p -> p.name + ": ...").collect(Collectors.toSet())),
                new DQLStatementInsertionHandler(command))
        );
    }

    public static void autocompleteConditionOperands(CompletionResultSet result) {
        for (String operand : new String[]{"and", "or", "xor"}) {
            result.addElement(AutocompleteUtils.createLookupElement(
                    operand,
                    DQLIcon.DQL_OPERAND,
                    AutocompleteUtils.STATIC,
                    null,
                    new DQLValueAfterOperandInsertionHandler(operand)
            ));
        }
    }

    public static void autocompleteInExpression(CompletionResultSet result) {
        result.addElement(AutocompleteUtils.createLookupElement(
                "in",
                DQLIcon.DQL_OPERAND,
                AutocompleteUtils.EXPRESSION,
                "[ ]",
                new DQLInExpressionInsertionHandler()
        ));
    }
}
