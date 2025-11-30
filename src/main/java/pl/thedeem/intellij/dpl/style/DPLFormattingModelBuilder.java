package pl.thedeem.intellij.dpl.style;

import com.intellij.formatting.*;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DynatracePatternLanguage;
import pl.thedeem.intellij.dpl.psi.DPLTypes;

public class DPLFormattingModelBuilder implements FormattingModelBuilder {
    @Override
    public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
        final CodeStyleSettings codeStyleSettings = formattingContext.getCodeStyleSettings();
        DPLCodeStyleSettings settings = codeStyleSettings.getCustomSettings(DPLCodeStyleSettings.class);
        return FormattingModelProvider
                .createFormattingModelForPsiFile(
                        formattingContext.getContainingFile(),
                        new DPLBlock(
                                formattingContext.getNode(),
                                Wrap.createWrap(WrapType.NONE, false),
                                Alignment.createAlignment(),
                                createSpaceBuilder(codeStyleSettings, settings),
                                Indent.getAbsoluteNoneIndent(),
                                settings
                        ),
                        codeStyleSettings
                );
    }

    private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings, DPLCodeStyleSettings langSettings) {
        SpacingBuilder spacingBuilder = new SpacingBuilder(settings, DynatracePatternLanguage.INSTANCE);
        spacingBuilder = getSettingsForCommands(spacingBuilder, langSettings);
        spacingBuilder = getSettingsForGroups(spacingBuilder, langSettings);
        spacingBuilder = getSettingsForExpressions(spacingBuilder, langSettings);
        return spacingBuilder;
    }

    private static SpacingBuilder getSettingsForCommands(SpacingBuilder base, DPLCodeStyleSettings langSettings) {
        SpacingBuilder result = base;

        // command matchers
        if (langSettings.FORCE_SETTINGS_WITHIN_MATCHERS) {
            result = result
                    .afterInside(DPLTypes.L_BRACE, DPLTypes.MATCHERS_EXPRESSION)
                    .lineBreakOrForceSpace(langSettings.LB_AROUND_BRACE_IN_MATCHER, langSettings.SPACE_AROUND_BRACE_IN_MATCHER)
                    .beforeInside(DPLTypes.R_BRACE, DPLTypes.MATCHERS_EXPRESSION)
                    .lineBreakOrForceSpace(langSettings.LB_AROUND_BRACE_IN_MATCHER, langSettings.SPACE_AROUND_BRACE_IN_MATCHER)
                    .afterInside(DPLTypes.COMMA, DPLTypes.MEMBERS_LIST_MATCHERS)
                    .lineBreakOrForceSpace(langSettings.LB_AROUND_EXPRESSIONS_IN_MATCHER, langSettings.SPACE_AROUND_EXPRESSIONS_IN_MATCHER)
                    .afterInside(DPLTypes.COMMA, DPLTypes.PARAMETERS_MATCHERS_LIST)
                    .lineBreakOrForceSpace(langSettings.LB_AROUND_EXPRESSIONS_IN_MATCHER, langSettings.SPACE_AROUND_EXPRESSIONS_IN_MATCHER)
                    .betweenInside(DPLTypes.EXPRESSION_DEFINITION, DPLTypes.EXPRESSION_DEFINITION, DPLTypes.EXPRESSION_MATCHERS_LIST)
                    .lineBreakOrForceSpace(langSettings.LB_AROUND_EXPRESSIONS_IN_MATCHER, langSettings.SPACE_AROUND_EXPRESSIONS_IN_MATCHER)
            ;
        } else {
            result = result
                    .afterInside(DPLTypes.L_BRACE, DPLTypes.MATCHERS_EXPRESSION)
                    .lineBreakInCodeIf(langSettings.LB_AROUND_BRACE_IN_MATCHER)
                    .beforeInside(DPLTypes.R_BRACE, DPLTypes.MATCHERS_EXPRESSION)
                    .lineBreakInCodeIf(langSettings.LB_AROUND_BRACE_IN_MATCHER)
                    .afterInside(DPLTypes.L_BRACE, DPLTypes.MATCHERS_EXPRESSION)
                    .spaceIf(langSettings.SPACE_AROUND_BRACE_IN_MATCHER)
                    .beforeInside(DPLTypes.R_BRACE, DPLTypes.MATCHERS_EXPRESSION)
                    .spaceIf(langSettings.SPACE_AROUND_BRACE_IN_MATCHER)

                    .afterInside(DPLTypes.COMMA, DPLTypes.MEMBERS_LIST_MATCHERS)
                    .lineBreakInCodeIf(langSettings.LB_AROUND_EXPRESSIONS_IN_MATCHER)
                    .afterInside(DPLTypes.COMMA, DPLTypes.MEMBERS_LIST_MATCHERS)
                    .spaceIf(langSettings.SPACE_AROUND_EXPRESSIONS_IN_MATCHER)
                    .afterInside(DPLTypes.COMMA, DPLTypes.PARAMETERS_MATCHERS_LIST)
                    .lineBreakInCodeIf(langSettings.LB_AROUND_EXPRESSIONS_IN_MATCHER)
                    .afterInside(DPLTypes.COMMA, DPLTypes.PARAMETERS_MATCHERS_LIST)
                    .spaceIf(langSettings.SPACE_AROUND_EXPRESSIONS_IN_MATCHER)
                    .betweenInside(DPLTypes.EXPRESSION_DEFINITION, DPLTypes.EXPRESSION_DEFINITION, DPLTypes.EXPRESSION_MATCHERS_LIST)
                    .lineBreakInCodeIf(langSettings.LB_AROUND_EXPRESSIONS_IN_MATCHER);
        }

        // defaults
        return result
                .beforeInside(DPLTypes.L_BRACE, DPLTypes.MATCHERS_EXPRESSION)
                .none()
                .around(DPLTypes.COMMAND_KEYWORD)
                .none();
    }


    private static SpacingBuilder getSettingsForExpressions(SpacingBuilder base, DPLCodeStyleSettings langSettings) {
        return base
                .betweenInside(DPLTypes.EXPRESSION_DEFINITION, DPLTypes.EXPRESSION_DEFINITION, DPLTypes.DPL)
                .lineBreakInCodeIf(langSettings.LB_BETWEEN_EXPRESSIONS)
                .afterInside(DPLTypes.L_BRACE, DPLTypes.LIMITED_QUANTIFIER)
                .spaceIf(langSettings.SPACE_INSIDE_COMPLEX_QUANTIFIER)
                .beforeInside(DPLTypes.R_BRACE, DPLTypes.LIMITED_QUANTIFIER)
                .spaceIf(langSettings.SPACE_INSIDE_COMPLEX_QUANTIFIER)
                .afterInside(DPLTypes.COMMA, DPLTypes.MIN_MAX_QUANTIFIER)
                .spaceIf(langSettings.SPACE_INSIDE_COMPLEX_QUANTIFIER)
                .aroundInside(DPLTypes.SET, DPLTypes.MACRO_DEFINITION_EXPRESSION)
                .spaceIf(langSettings.SPACE_AROUND_SET_IN_MACRO_DEFINITION)
                .after(DPLTypes.MACRO_DEFINITION_EXPRESSION)
                .lineBreakInCodeIf(langSettings.LB_AFTER_MACRO_DEFINITION)

                // expression parameters
                .aroundInside(DPLTypes.SET, DPLTypes.PARAMETER_EXPRESSION)
                .spaceIf(langSettings.SPACE_AROUND_CONFIGURATION_SET)
                .afterInside(DPLTypes.L_PAREN, DPLTypes.CONFIGURATION_EXPRESSION)
                .spaceIf(langSettings.SPACE_AROUND_CONFIGURATION_PARENTHESES)
                .beforeInside(DPLTypes.R_PAREN, DPLTypes.CONFIGURATION_EXPRESSION)
                .spaceIf(langSettings.SPACE_AROUND_CONFIGURATION_PARENTHESES)
                .afterInside(DPLTypes.COMMA, DPLTypes.CONFIGURATION_CONTENT)
                .spaceIf(langSettings.SPACE_AFTER_CONFIGURATION_PARAMETERS)
                .afterInside(DPLTypes.COMMA, DPLTypes.CONFIGURATION_CONTENT)
                .lineBreakInCodeIf(langSettings.LB_AFTER_CONFIGURATION_PARAMETERS)

                // default spacing
                .between(DPLTypes.EXPRESSION_DEFINITION, DPLTypes.EXPRESSION_DEFINITION)
                .spaces(1)
                .aroundInside(DPLTypes.COLON, DPLTypes.EXPORT_NAME_EXPRESSION)
                .none()
                .around(DPLTypes.LOOKAROUND_EXPRESSION)
                .none()
                .beforeInside(DPLTypes.L_PAREN, DPLTypes.CONFIGURATION_EXPRESSION)
                .none()
                .beforeInside(DPLTypes.LIMITED_QUANTIFIER, DPLTypes.QUANTIFIER_EXPRESSION)
                .none()
                .beforeInside(DPLTypes.SIMPLE_QUANTIFIER, DPLTypes.QUANTIFIER_EXPRESSION)
                .none()
                .around(DPLTypes.NULLABLE_EXPRESSION)
                .none()
                ;
    }

    private static SpacingBuilder getSettingsForGroups(SpacingBuilder base, DPLCodeStyleSettings langSettings) {
        SpacingBuilder result = base;
        if (langSettings.FORCE_SETTINGS_FOR_GROUPS) {
            result = result
                    .afterInside(DPLTypes.L_PAREN, DPLTypes.GROUP_EXPRESSION)
                    .lineBreakOrForceSpace(langSettings.LB_AROUND_PARENTHESES_IN_GROUPS, langSettings.SPACE_AROUND_PARENTHESES_IN_GROUPS)
                    .beforeInside(DPLTypes.R_PAREN, DPLTypes.GROUP_EXPRESSION)
                    .lineBreakOrForceSpace(langSettings.LB_AROUND_PARENTHESES_IN_GROUPS, langSettings.SPACE_AROUND_PARENTHESES_IN_GROUPS)
                    .betweenInside(DPLTypes.EXPRESSION_DEFINITION, DPLTypes.EXPRESSION_DEFINITION, DPLTypes.ALTERNATIVES_EXPRESSION)
                    .lineBreakOrForceSpace(langSettings.LB_BETWEEN_EXPRESSIONS_IN_GROUPS, true);

            result = result
                    .beforeInside(DPLTypes.OR, DPLTypes.ALTERNATIVES_EXPRESSION)
                    .lineBreakOrForceSpace(langSettings.LB_BETWEEN_EXPRESSIONS_IN_GROUPS, true);
        } else {
            result = result
                    .afterInside(DPLTypes.L_PAREN, DPLTypes.GROUP_EXPRESSION)
                    .lineBreakInCodeIf(langSettings.LB_AROUND_PARENTHESES_IN_GROUPS)
                    .afterInside(DPLTypes.L_PAREN, DPLTypes.GROUP_EXPRESSION)
                    .spaceIf(langSettings.SPACE_AROUND_PARENTHESES_IN_GROUPS)
                    .afterInside(DPLTypes.L_PAREN, DPLTypes.GROUP_EXPRESSION)
                    .lineBreakInCodeIf(langSettings.LB_AROUND_PARENTHESES_IN_GROUPS)
                    .beforeInside(DPLTypes.R_PAREN, DPLTypes.GROUP_EXPRESSION)
                    .lineBreakInCodeIf(langSettings.LB_AROUND_PARENTHESES_IN_GROUPS);

            result = result
                    .beforeInside(DPLTypes.OR, DPLTypes.ALTERNATIVES_EXPRESSION)
                    .lineBreakInCodeIf(langSettings.LB_BETWEEN_EXPRESSIONS_IN_GROUPS);
        }

        result = result
                .aroundInside(DPLTypes.OR, DPLTypes.ALTERNATIVES_EXPRESSION)
                .spaceIf(langSettings.SPACE_AROUND_GROUP_OR_OPERATOR);
        return result;
    }
}
