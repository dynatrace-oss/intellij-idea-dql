package pl.thedeem.intellij.dql.style;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;

public class DQLLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
    @NotNull
    @Override
    public Language getLanguage() {
        return DynatraceQueryLanguage.INSTANCE;
    }

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_BEFORE_BRACKETS_COMMA", DQLBundle.message("settings.style.spaceBeforeFieldsComma"), DQLBundle.message("settings.style.groups.bracket"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_AFTER_BRACKETS_COMMA", DQLBundle.message("settings.style.spaceAfterFieldsComma"), DQLBundle.message("settings.style.groups.bracket"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_INSIDE_BRACES", DQLBundle.message("settings.style.spaceInsideBraces"), DQLBundle.message("settings.style.groups.bracket"));

            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_BEFORE_PARAMETER_COMMA", DQLBundle.message("settings.style.spaceBeforeArgumentsComma"), DQLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_BEFORE_PARAMETER", DQLBundle.message("settings.style.spaceBetweenParameters"), DQLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_BEFORE_PIPE", DQLBundle.message("settings.style.spaceBeforePipe"), DQLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_AFTER_PIPE", DQLBundle.message("settings.style.spaceAfterPipe"), DQLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "INDENT_BEFORE_PIPE", DQLBundle.message("settings.style.indentQueryCommands"), DQLBundle.message("settings.style.groups.commands"));

            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_BEFORE_FUNCTION_ARGUMENTS_COMMA", DQLBundle.message("settings.style.spaceBeforeFunctionArgumentsComma"), DQLBundle.message("settings.style.groups.functions"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_BETWEEN_FUNCTION_PARAMETERS", DQLBundle.message("settings.style.spaceBetweenFunctionParams"), DQLBundle.message("settings.style.groups.functions"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "WHITESPACE_BEFORE_FUNCTION_PARAMETERS", DQLBundle.message("settings.style.spaceBeforeFunctionParameters"), DQLBundle.message("settings.style.groups.functions"));

            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_BEFORE_COLON", DQLBundle.message("settings.style.spaceBeforeColon"), DQLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_AFTER_COLON", DQLBundle.message("settings.style.spaceAfterColon"), DQLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_AROUND_EQUALITY_OPERATORS", DQLBundle.message("settings.style.spaceAroundEqualityOperator"), DQLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_AROUND_SEARCH_OPERATORS", DQLBundle.message("settings.style.spaceAroundSearchOperator"), DQLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_AROUND_ARITHMETIC_OPERATORS", DQLBundle.message("settings.style.spaceAroundArithmeticOperator"), DQLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_AROUND_COMPARISON_OPERATORS", DQLBundle.message("settings.style.spaceAroundComparisonOperator"), DQLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_AROUND_ASSIGNMENT", DQLBundle.message("settings.style.spaceAroundAssignmentOperator"), DQLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_INSIDE_SUBQUERY", DQLBundle.message("settings.style.spaceInsideSubquery"), DQLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_INSIDE_FUNCTION_PARENTHESES", DQLBundle.message("settings.style.spaceInsideFunction"), DQLBundle.message("settings.style.groups.expressions"));

            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACE_BEFORE_INLINE_COMMENTS", DQLBundle.message("settings.style.spaceBeforeComments"), DQLBundle.message("settings.style.groups.comments"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SPACES_BETWEEN_COMMENT_TOKENS", DQLBundle.message("settings.style.spacesBetweenCommentTokens"), DQLBundle.message("settings.style.groups.comments"));
        } else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
            consumer.showCustomOption(DQLCodeStyleSettings.class, "WRAP_LONG_EXPRESSIONS", DQLBundle.message("settings.style.wrapLongExpressions"), DQLBundle.message("settings.style.groups.default"));

            consumer.showCustomOption(DQLCodeStyleSettings.class, "REFORMAT_DPL_FRAGMENTS", DQLBundle.message("settings.style.reformatDpl"), DQLBundle.message("settings.style.groups.injectedLanguages"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "REFORMAT_JSON_FRAGMENTS", DQLBundle.message("settings.style.reformatJson"), DQLBundle.message("settings.style.groups.injectedLanguages"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "SURROUND_INJECTED_FRAGMENTS_WITH_NEW_LINES", DQLBundle.message("settings.style.surroundFragmentsWithNewLines"), DQLBundle.message("settings.style.groups.injectedLanguages"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "KEEP_INDENT_FOR_INJECTED_FRAGMENTS", DQLBundle.message("settings.style.keepIndentForInjectedFragments"), DQLBundle.message("settings.style.groups.injectedLanguages"));

            consumer.showCustomOption(DQLCodeStyleSettings.class, "FORCE_LB_SETTINGS_FOR_BRACKETS", DQLBundle.message("settings.style.forceLbSettingsForBrackets"), DQLBundle.message("settings.style.groups.bracket"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "LB_BEFORE_FIELDS_COMMA", DQLBundle.message("settings.style.lbBeforeFieldsComma"), DQLBundle.message("settings.style.groups.bracket"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "LB_AFTER_FIELDS_COMMA", DQLBundle.message("settings.style.lbAfterFieldsComma"), DQLBundle.message("settings.style.groups.bracket"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "LB_AFTER_L_BRACE", DQLBundle.message("settings.style.lbAfterOpeningBrace"), DQLBundle.message("settings.style.groups.bracket"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "LB_BEFORE_R_BRACE", DQLBundle.message("settings.style.lbBeforeClosingBrace"), DQLBundle.message("settings.style.groups.bracket"));

            consumer.showCustomOption(DQLCodeStyleSettings.class, "FORCE_LB_SETTINGS_FOR_COMMAND_PARAMETERS", DQLBundle.message("settings.style.forceLbSettingsForCommandParameters"), DQLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "LB_BEFORE_PIPE", DQLBundle.message("settings.style.lbBeforePipe"), DQLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "LB_BEFORE_ARGUMENT_COMMA", DQLBundle.message("settings.style.lbBeforeArgumentsComma"), DQLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "LB_BEFORE_COMMAND_PARAM", DQLBundle.message("settings.style.lbBeforeParameter"), DQLBundle.message("settings.style.groups.commands"));

            consumer.showCustomOption(DQLCodeStyleSettings.class, "FORCE_LB_SETTINGS_FOR_FUNCTION_PARAMETERS", DQLBundle.message("settings.style.forceLbSettingsForFunctionParameters"), DQLBundle.message("settings.style.groups.functions"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "LB_BEFORE_FUNCTION_ARGUMENTS_COMMA", DQLBundle.message("settings.style.lbBeforeFunctionArgumentsComma"), DQLBundle.message("settings.style.groups.functions"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "LB_INSIDE_FUNCTION_ARGUMENTS_LIST", DQLBundle.message("settings.style.lbBeforeFunctionParams"), DQLBundle.message("settings.style.groups.functions"));

            consumer.showCustomOption(DQLCodeStyleSettings.class, "LB_AFTER_ASSIGNMENT", DQLBundle.message("settings.style.lbAfterAssignment"), DQLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "LB_SUBQUERY", DQLBundle.message("settings.style.lbInsideSubqueries"), DQLBundle.message("settings.style.groups.expressions"));

            consumer.showCustomOption(DQLCodeStyleSettings.class, "FORCE_SETTINGS_FOR_COMMENTS", DQLBundle.message("settings.style.forceCommentsSettings"), DQLBundle.message("settings.style.groups.comments"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "LB_BEFORE_INLINE_COMMENTS", DQLBundle.message("settings.style.lbBeforeInlineComment"), DQLBundle.message("settings.style.groups.comments"));
            consumer.showCustomOption(DQLCodeStyleSettings.class, "LB_AROUND_BLOCK_COMMENTS", DQLBundle.message("settings.style.lbAroundBlockComments"), DQLBundle.message("settings.style.groups.comments"));
        } else if (settingsType == SettingsType.INDENT_SETTINGS) {
            consumer.showStandardOptions("INDENT_SIZE", "CONTINUATION_INDENT_SIZE", "TAB_SIZE", "USE_TAB_CHARACTER");
        }
    }

    @Override
    public int getRightMargin(@NotNull SettingsType settingsType) {
        return settingsType == LanguageCodeStyleSettingsProvider.SettingsType.WRAPPING_AND_BRACES_SETTINGS ? 90 : -1;
    }

    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        return /* language=DQL */ """
                data record(my.favourite.language = "Dynatrace Query Language")
                | fieldsAdd {
                    size = stringLength(my.favourite.language),
                    other_example = 10 * power(10, 100),
                    `Can you have named fields?` = true,
                    variable_value = $someVariable // dashboard variable
                }
                | fieldsAdd {
                   additionalField = "DQL is awesome language!" ~ "DQL"
                }
                // and this is a comment
                | filter (matchesValue(my.favourite.language, "*Dynatrace*", caseSensitive: false) and (55 * other_example) >= size) or (other_example == 10)
                | dedup my.favourite.language, sort: size desc
                /*
                  Here is a multiline block comment
                  You can specify here multiple lines
                */
                | append [
                    fetch logs, from: now() - 5d, to: now() - 1d
                    | parse content, "TIMESTAMP:t ' ' UPPER:severity ' ' LDATA:message"
                ]
                """;
    }

    @Override
    public void customizeDefaults(@NotNull CommonCodeStyleSettings commonSettings,
                                  @NotNull CommonCodeStyleSettings.IndentOptions indentOptions) {
        indentOptions.INDENT_SIZE = 2;
        indentOptions.CONTINUATION_INDENT_SIZE = 4;
        indentOptions.TAB_SIZE = 1;
        indentOptions.USE_TAB_CHARACTER = false;
    }


    @Override
    public IndentOptionsEditor getIndentOptionsEditor() {
        return new SmartIndentOptionsEditor();
    }
}