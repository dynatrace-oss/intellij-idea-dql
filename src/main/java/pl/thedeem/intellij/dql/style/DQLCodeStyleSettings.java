package pl.thedeem.intellij.dql.style;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

public class DQLCodeStyleSettings extends CustomCodeStyleSettings {
    public boolean SURROUND_INJECTED_FRAGMENTS_WITH_NEW_LINES = true;
    public boolean KEEP_INDENT_FOR_INJECTED_FRAGMENTS = true;
    public boolean REFORMAT_DPL_FRAGMENTS = true;
    public boolean REFORMAT_JSON_FRAGMENTS = true;
    public boolean WRAP_LONG_EXPRESSIONS = true;
    public boolean SPACE_AFTER_PIPE = true;
    public boolean SPACE_BEFORE_PIPE = true;
    public boolean SPACE_AFTER_COLON = true;
    public boolean SPACE_BEFORE_PARAMETER = true;
    public boolean SPACE_BEFORE_COLON = false;
    public boolean SPACE_BEFORE_PARAMETER_COMMA = false;
    public boolean SPACE_BEFORE_BRACKETS_COMMA = false;
    public boolean SPACE_AFTER_BRACKETS_COMMA = true;
    public boolean SPACE_BEFORE_FUNCTION_ARGUMENTS_COMMA = false;
    public boolean SPACE_INSIDE_BRACES = true;
    public boolean SPACE_INSIDE_FUNCTION_PARENTHESES = false;
    public boolean SPACE_AROUND_EQUALITY_OPERATORS = true;
    public boolean SPACE_AROUND_COMPARISON_OPERATORS = true;
    public boolean SPACE_AROUND_ARITHMETIC_OPERATORS = true;
    public boolean SPACE_BETWEEN_FUNCTION_PARAMETERS = true;
    public boolean SPACE_INSIDE_SUBQUERY = false;
    public boolean SPACE_AROUND_ASSIGNMENT = true;
    public boolean WHITESPACE_BEFORE_FUNCTION_PARAMETERS = false;

    public boolean LB_INSIDE_FUNCTION_ARGUMENTS_LIST = false;
    public boolean LB_BEFORE_PIPE = true;
    public boolean LB_AFTER_L_BRACE = true;
    public boolean LB_BEFORE_R_BRACE = true;
    public boolean LB_BEFORE_COMMAND_PARAM = false;
    public boolean LB_AFTER_ASSIGNMENT = false;
    public boolean LB_SUBQUERY = true;
    public boolean LB_BEFORE_ARGUMENT_COMMA = false;
    public boolean LB_BEFORE_FIELDS_COMMA = false;
    public boolean LB_AFTER_FIELDS_COMMA = false;
    public boolean LB_BEFORE_FUNCTION_ARGUMENTS_COMMA = false;

    public boolean FORCE_LB_SETTINGS_FOR_BRACKETS = false;
    public boolean FORCE_LB_SETTINGS_FOR_COMMAND_PARAMETERS = false;
    public boolean FORCE_LB_SETTINGS_FOR_FUNCTION_PARAMETERS = false;

    public boolean FORCE_SETTINGS_FOR_COMMENTS = false;
    public boolean SPACE_BEFORE_INLINE_COMMENTS = true;
    public boolean LB_BEFORE_INLINE_COMMENTS = false;
    public boolean LB_AROUND_BLOCK_COMMENTS = false;
    public boolean SPACES_BETWEEN_COMMENT_TOKENS = true;

    public boolean INDENT_BEFORE_PIPE = false;

    public DQLCodeStyleSettings(CodeStyleSettings settings) {
        super("DQLCodeStyleSettings", settings);
    }
}
