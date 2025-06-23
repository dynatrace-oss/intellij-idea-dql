package pl.thedeem.intellij.dql.style;

import com.intellij.formatting.*;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.psi.DQLTokenSets;
import pl.thedeem.intellij.dql.psi.DQLTypes;
import org.jetbrains.annotations.NotNull;


public class DQLFormattingModelBuilder implements FormattingModelBuilder {
    @Override
    public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
        final CodeStyleSettings codeStyleSettings = formattingContext.getCodeStyleSettings();
        DQLCodeStyleSettings dqlSettings = codeStyleSettings.getCustomSettings(DQLCodeStyleSettings.class);
        return FormattingModelProvider
                .createFormattingModelForPsiFile(
                        formattingContext.getContainingFile(),
                        new DQLBlock(
                                formattingContext.getNode(),
                                Wrap.createWrap(WrapType.NONE, false),
                                Alignment.createAlignment(),
                                createSpaceBuilder(codeStyleSettings),
                                Indent.getAbsoluteNoneIndent(),
                                dqlSettings
                        ),
                        codeStyleSettings
                );
    }

    private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings) {
        DQLCodeStyleSettings dqlSettings = settings.getCustomSettings(DQLCodeStyleSettings.class);
        SpacingBuilder spacingBuilder = new SpacingBuilder(settings, DynatraceQueryLanguage.INSTANCE);
        spacingBuilder = createEnforcedOptions(spacingBuilder);
        spacingBuilder = handleBracketSettings(spacingBuilder, dqlSettings);
        spacingBuilder = handleCommandsSettings(spacingBuilder, dqlSettings);
        spacingBuilder = handleFunctionsSettings(spacingBuilder, dqlSettings);
        spacingBuilder = handleExpressionsSettings(spacingBuilder, dqlSettings);

        return spacingBuilder;
    }

    private static SpacingBuilder handleBracketSettings(SpacingBuilder builder, DQLCodeStyleSettings dqlSettings) {
        SpacingBuilder result = builder;
        if (dqlSettings.FORCE_LB_SETTINGS_FOR_BRACKETS) {
            result = result
                    .after(DQLTypes.L_BRACE)
                    .lineBreakOrForceSpace(dqlSettings.LB_AFTER_L_BRACE, dqlSettings.SPACE_INSIDE_BRACES)
                    .before(DQLTypes.R_BRACE)
                    .lineBreakOrForceSpace(dqlSettings.LB_BEFORE_R_BRACE, dqlSettings.SPACE_INSIDE_BRACES)

                    .beforeInside(DQLTypes.COMMA, DQLTypes.BRACKET_EXPRESSION)
                    .lineBreakOrForceSpace(dqlSettings.LB_BEFORE_FIELDS_COMMA, dqlSettings.SPACE_BEFORE_BRACKETS_COMMA)
                    .afterInside(DQLTypes.COMMA, DQLTypes.BRACKET_EXPRESSION)
                    .lineBreakOrForceSpace(dqlSettings.LB_AFTER_FIELDS_COMMA, dqlSettings.SPACE_AFTER_BRACKETS_COMMA)
            ;
        } else {
            result = result
                    .after(DQLTypes.L_BRACE)
                    .lineBreakInCodeIf(dqlSettings.LB_AFTER_L_BRACE)
                    .after(DQLTypes.L_BRACE)
                    .spaceIf(dqlSettings.SPACE_INSIDE_BRACES)

                    .after(DQLTypes.R_BRACE)
                    .lineBreakInCodeIf(dqlSettings.LB_BEFORE_R_BRACE)
                    .after(DQLTypes.R_BRACE)
                    .spaceIf(dqlSettings.SPACE_INSIDE_BRACES)

                    .beforeInside(DQLTypes.COMMA, DQLTypes.BRACKET_EXPRESSION)
                    .lineBreakInCodeIf(dqlSettings.LB_BEFORE_FIELDS_COMMA)
                    .beforeInside(DQLTypes.COMMA, DQLTypes.BRACKET_EXPRESSION)
                    .spaceIf(dqlSettings.SPACE_BEFORE_BRACKETS_COMMA)

                    .afterInside(DQLTypes.COMMA, DQLTypes.BRACKET_EXPRESSION)
                    .lineBreakInCodeIf(dqlSettings.LB_AFTER_FIELDS_COMMA)
                    .afterInside(DQLTypes.COMMA, DQLTypes.BRACKET_EXPRESSION)
                    .spaceIf(dqlSettings.SPACE_AFTER_BRACKETS_COMMA);
        }
        return result;
    }

    private static SpacingBuilder handleCommandsSettings(SpacingBuilder builder, DQLCodeStyleSettings dqlSettings) {
        SpacingBuilder result = builder;
        if (dqlSettings.FORCE_LB_SETTINGS_FOR_COMMAND_PARAMETERS) {
            result = result
                    .beforeInside(DQLTypes.COMMA, DQLTypes.QUERY_STATEMENT)
                    .lineBreakOrForceSpace(dqlSettings.LB_BEFORE_ARGUMENT_COMMA, dqlSettings.SPACE_BEFORE_PARAMETER_COMMA)
                    .afterInside(DQLTypes.COMMA, DQLTypes.QUERY_STATEMENT)
                    .lineBreakOrForceSpace(dqlSettings.LB_BEFORE_COMMAND_PARAM, dqlSettings.SPACE_BEFORE_PARAMETER);
        } else {
            result = result
                    .beforeInside(DQLTypes.COMMA, DQLTypes.QUERY_STATEMENT)
                    .lineBreakInCodeIf(dqlSettings.LB_BEFORE_ARGUMENT_COMMA)
                    .beforeInside(DQLTypes.COMMA, DQLTypes.QUERY_STATEMENT)
                    .spaceIf(dqlSettings.SPACE_BEFORE_PARAMETER_COMMA)

                    .afterInside(DQLTypes.COMMA, DQLTypes.QUERY_STATEMENT)
                    .lineBreakInCodeIf(dqlSettings.LB_BEFORE_COMMAND_PARAM)
                    .afterInside(DQLTypes.COMMA, DQLTypes.QUERY_STATEMENT)
                    .spaceIf(dqlSettings.SPACE_BEFORE_PARAMETER);
        }
        return result
                .after(DQLTypes.QUERY_STATEMENT)
                .lineBreakInCodeIf(dqlSettings.LB_BEFORE_PIPE)
                .after(DQLTypes.QUERY_STATEMENT)
                .spaceIf(dqlSettings.SPACE_BEFORE_PIPE)
                .after(DQLTypes.PIPE)
                .spaceIf(dqlSettings.SPACE_AFTER_PIPE);
    }

    private static SpacingBuilder handleFunctionsSettings(SpacingBuilder builder, DQLCodeStyleSettings dqlSettings) {
        SpacingBuilder result = builder;
        if (dqlSettings.FORCE_LB_SETTINGS_FOR_FUNCTION_PARAMETERS) {
            result = result
                    .beforeInside(DQLTypes.COMMA, DQLTypes.FUNCTION_CALL_EXPRESSION)
                    .lineBreakOrForceSpace(dqlSettings.LB_BEFORE_FUNCTION_ARGUMENTS_COMMA, dqlSettings.SPACE_BEFORE_FUNCTION_ARGUMENTS_COMMA)
                    .afterInside(DQLTypes.COMMA, DQLTypes.FUNCTION_CALL_EXPRESSION)
                    .lineBreakOrForceSpace(dqlSettings.LB_INSIDE_FUNCTION_ARGUMENTS_LIST, dqlSettings.SPACE_BETWEEN_FUNCTION_PARAMETERS)
                    .beforeInside(DQLTypes.R_PARENTHESIS, DQLTypes.FUNCTION_CALL_EXPRESSION)
                    .lineBreakOrForceSpace(dqlSettings.LB_INSIDE_FUNCTION_ARGUMENTS_LIST, dqlSettings.SPACE_INSIDE_FUNCTION_PARENTHESES)
                    .afterInside(DQLTypes.L_PARENTHESIS, DQLTypes.FUNCTION_CALL_EXPRESSION)
                    .lineBreakOrForceSpace(dqlSettings.LB_INSIDE_FUNCTION_ARGUMENTS_LIST, dqlSettings.SPACE_INSIDE_FUNCTION_PARENTHESES)
            ;
        } else {
            result = result
                    .beforeInside(DQLTypes.COMMA, DQLTypes.FUNCTION_CALL_EXPRESSION)
                    .lineBreakInCodeIf(dqlSettings.LB_BEFORE_FUNCTION_ARGUMENTS_COMMA)
                    .beforeInside(DQLTypes.COMMA, DQLTypes.FUNCTION_CALL_EXPRESSION)
                    .spaceIf(dqlSettings.SPACE_BEFORE_FUNCTION_ARGUMENTS_COMMA)

                    .afterInside(DQLTypes.COMMA, DQLTypes.FUNCTION_CALL_EXPRESSION)
                    .lineBreakInCodeIf(dqlSettings.LB_INSIDE_FUNCTION_ARGUMENTS_LIST)
                    .afterInside(DQLTypes.COMMA, DQLTypes.FUNCTION_CALL_EXPRESSION)
                    .spaceIf(dqlSettings.SPACE_BETWEEN_FUNCTION_PARAMETERS)

                    .afterInside(DQLTypes.L_PARENTHESIS, DQLTypes.FUNCTION_CALL_EXPRESSION)
                    .lineBreakInCodeIf(dqlSettings.LB_INSIDE_FUNCTION_ARGUMENTS_LIST)
                    .afterInside(DQLTypes.L_PARENTHESIS, DQLTypes.FUNCTION_CALL_EXPRESSION)
                    .spaceIf(dqlSettings.SPACE_INSIDE_FUNCTION_PARENTHESES)
                    .beforeInside(DQLTypes.R_PARENTHESIS, DQLTypes.FUNCTION_CALL_EXPRESSION)
                    .lineBreakInCodeIf(dqlSettings.LB_INSIDE_FUNCTION_ARGUMENTS_LIST)
                    .beforeInside(DQLTypes.R_PARENTHESIS, DQLTypes.FUNCTION_CALL_EXPRESSION)
                    .spaceIf(dqlSettings.SPACE_INSIDE_FUNCTION_PARENTHESES);
        }
        return result
                .after(DQLTypes.FUNCTION_NAME)
                .spaceIf(dqlSettings.WHITESPACE_BEFORE_FUNCTION_PARAMETERS);
    }

    private static SpacingBuilder handleExpressionsSettings(SpacingBuilder builder, DQLCodeStyleSettings dqlSettings) {
        return builder
                .before(DQLTypes.COLON)
                .spaceIf(dqlSettings.SPACE_BEFORE_COLON)
                .after(DQLTypes.COLON)
                .spaceIf(dqlSettings.SPACE_AFTER_COLON)

                .around(DQLTypes.EQUALITY_OPERATOR)
                .spaceIf(dqlSettings.SPACE_AROUND_EQUALITY_OPERATORS)
                .around(DQLTypes.MULTIPLICATIVE_OPERATOR)
                .spaceIf(dqlSettings.SPACE_AROUND_ARITHMETIC_OPERATORS)
                .around(DQLTypes.ADDITIVE_OPERATOR)
                .spaceIf(dqlSettings.SPACE_AROUND_ARITHMETIC_OPERATORS)
                .around(DQLTypes.COMPARISON_OPERATOR)
                .spaceIf(dqlSettings.SPACE_AROUND_COMPARISON_OPERATORS)
                .before(DQLTypes.ASSIGNMENT_OPERATOR)
                .spaceIf(dqlSettings.SPACE_AROUND_ASSIGNMENT)
                .after(DQLTypes.ASSIGNMENT_OPERATOR)
                .lineBreakOrForceSpace(dqlSettings.LB_AFTER_ASSIGNMENT, dqlSettings.SPACE_AROUND_ASSIGNMENT)
                .afterInside(DQLTypes.ARRAY_OPEN, DQLTypes.SUBQUERY_EXPRESSION)
                .lineBreakOrForceSpace(dqlSettings.LB_SUBQUERY, dqlSettings.SPACE_INSIDE_SUBQUERY)
                ;
    }

    private static SpacingBuilder createEnforcedOptions(SpacingBuilder builder) {
        return builder.around(DQLTokenSets.WORD_EXPRESSION_OPERATORS) // operators like "and", "or", "not" always need spaces around
                .spaces(1)
                .after(DQLTypes.EOL_COMMENT) // the comment needs to always be the last element in the line
                .lineBreakInCode();
    }
}
