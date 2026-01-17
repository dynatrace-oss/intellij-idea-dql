package pl.thedeem.intellij.dpl.style;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

public class DPLCodeStyleSettings extends CustomCodeStyleSettings {
    public boolean WRAP_LONG_EXPRESSIONS = true;
    public boolean FORCE_SETTINGS_WITHIN_MATCHERS = false;
    public boolean LB_AROUND_BRACE_IN_MATCHER = false;
    public boolean LB_AROUND_EXPRESSIONS_IN_MATCHER = false;
    public boolean SPACE_AROUND_BRACE_IN_MATCHER = true;
    public boolean SPACE_AROUND_EXPRESSIONS_IN_MATCHER = true;
    public boolean SPACE_INSIDE_COMPLEX_QUANTIFIER = true;
    public boolean LB_BETWEEN_EXPRESSIONS = false;
    public boolean FORCE_SETTINGS_FOR_GROUPS = false;
    public boolean LB_BETWEEN_EXPRESSIONS_IN_GROUPS = false;
    public boolean LB_AROUND_PARENTHESES_IN_GROUPS = false;
    public boolean SPACE_AROUND_PARENTHESES_IN_GROUPS = false;
    public boolean SPACE_AROUND_GROUP_OR_OPERATOR = true;
    public boolean SPACE_AROUND_CONFIGURATION_SET = true;
    public boolean SPACE_AROUND_CONFIGURATION_PARENTHESES = false;
    public boolean SPACE_INSIDE_META_DIAMONDS = false;
    public boolean SPACE_AROUND_META_DIAMONDS = false;
    public boolean SPACE_AFTER_CONFIGURATION_PARAMETERS = true;
    public boolean LB_AFTER_CONFIGURATION_PARAMETERS = false;
    public boolean LB_AFTER_MACRO_DEFINITION = true;
    public boolean SPACE_AROUND_SET_IN_MACRO_DEFINITION = true;

    public DPLCodeStyleSettings(CodeStyleSettings settings) {
        super("DPLCodeStyleSettings", settings);
    }
}
