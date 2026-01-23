package pl.thedeem.intellij.dpl.style;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DynatracePatternLanguage;

public class DPLLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
    @NotNull
    @Override
    public Language getLanguage() {
        return DynatracePatternLanguage.INSTANCE;
    }

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showCustomOption(DPLCodeStyleSettings.class, "SPACE_AROUND_BRACE_IN_MATCHER", DPLBundle.message("settings.style.spaceAroundMatchersBraces"), DPLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "SPACE_AROUND_EXPRESSIONS_IN_MATCHER", DPLBundle.message("settings.style.spaceAroundMatchers"), DPLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "SPACE_AROUND_CONFIGURATION_PARENTHESES", DPLBundle.message("settings.style.spaceAroundConfiguration"), DPLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "SPACE_AFTER_CONFIGURATION_PARAMETERS", DPLBundle.message("settings.style.spaceAfterConfigurationParameters"), DPLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "SPACE_INSIDE_COMPLEX_QUANTIFIER", DPLBundle.message("settings.style.spaceInsideComplexQuantifiers"), DPLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "SPACE_AROUND_CONFIGURATION_SET", DPLBundle.message("settings.style.spaceAroundConfigurationSet"), DPLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "SPACE_AROUND_SET_IN_MACRO_DEFINITION", DPLBundle.message("settings.style.spaceAroundMacroDefinitionSet"), DPLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "SPACE_INSIDE_META_DIAMONDS", DPLBundle.message("settings.style.spaceInsideMetaDiamonds"), DPLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "SPACE_AROUND_PARENTHESES_IN_GROUPS", DPLBundle.message("settings.style.spaceAroundGroupParentheses"), DPLBundle.message("settings.style.groups.groups"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "SPACE_AROUND_GROUP_OR_OPERATOR", DPLBundle.message("settings.style.spaceAroundGroupOrOperator"), DPLBundle.message("settings.style.groups.groups"));
        } else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
            consumer.showCustomOption(DPLCodeStyleSettings.class, "WRAP_LONG_EXPRESSIONS", DPLBundle.message("settings.style.wrapLongExpressions"), DPLBundle.message("settings.style.groups.default"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "FORCE_SETTINGS_WITHIN_MATCHERS", DPLBundle.message("settings.style.forceLbWithinMatchers"), DPLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "LB_AROUND_BRACE_IN_MATCHER", DPLBundle.message("settings.style.lbWithinMatchersBraces"), DPLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "LB_AROUND_EXPRESSIONS_IN_MATCHER", DPLBundle.message("settings.style.lbWithinMatchers"), DPLBundle.message("settings.style.groups.commands"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "LB_BETWEEN_EXPRESSIONS", DPLBundle.message("settings.style.lbForExpressions"), DPLBundle.message("settings.style.groups.default"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "FORCE_SETTINGS_FOR_GROUPS", DPLBundle.message("settings.style.forceGroupSettings"), DPLBundle.message("settings.style.groups.groups"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "LB_AROUND_PARENTHESES_IN_GROUPS", DPLBundle.message("settings.style.lbBetweenGroupParentheses"), DPLBundle.message("settings.style.groups.groups"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "LB_BETWEEN_EXPRESSIONS_IN_GROUPS", DPLBundle.message("settings.style.lbBetweenGroupExpressions"), DPLBundle.message("settings.style.groups.groups"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "LB_AFTER_MACRO_DEFINITION", DPLBundle.message("settings.style.lbAfterMacroDefinition"), DPLBundle.message("settings.style.groups.expressions"));
            consumer.showCustomOption(DPLCodeStyleSettings.class, "LB_AFTER_CONFIGURATION_PARAMETERS", DPLBundle.message("settings.style.lbAfterConfigurationParameters"), DPLBundle.message("settings.style.groups.expressions"));
        }
    }

    @Override
    public int getRightMargin(@NotNull SettingsType settingsType) {
        return settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS ? 80 : -1;
    }

    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        return """
                /* an example DPL pattern */
                $macro1 = (INT* 'some string' DIGIT{1,10})?:sequence;$macro2 = (LD | ENUM{'success'=1, 'error'=2}):alternative_group;
                JSON{ IPADDR:"ip",INT:"port",TIMESTAMP('yyyy-MM-dd HH:mm:ss Z'):"client time" }(greedy='other members'):host // a comment
                KVP{ [a-z]:key '='{2} INT:value ' '? }:attr TIMESTAMP:date_time ','{,1} IPADDR:ip           ','+ LD:username         ','{1,} LD
                !<<IPADDR:ip_addr <<INT(min=50,max=100) (('1' <true>:is_valid) | ('0' <true>:is_valid)) EOL
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