package pl.thedeem.intellij.dql.highlighting;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class DQLColorScheme {
    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("DQL_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey STATEMENT_KEYWORD =
            createTextAttributesKey("DQL_STATEMENT_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey OPERATOR =
            createTextAttributesKey("DQL_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey OPERATOR_KEYWORDS =
            createTextAttributesKey("DQL_OPERATOR_KEYWORDS", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("DQL_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("DQL_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("DQL_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("DQL_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey BOOLEAN =
            createTextAttributesKey("DQL_BOOLEAN", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("DQL_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey FUNCTION =
            createTextAttributesKey("DQL_FUNCTION", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey PARAMETER =
            createTextAttributesKey("DQL_PARAMETER", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey FUNCTION_PARAMETER =
            createTextAttributesKey("DQL_FUNCTION_PARAMETER", DefaultLanguageHighlighterColors.REASSIGNED_PARAMETER);
    public static final TextAttributesKey DATA_FIELD =
            createTextAttributesKey("DQL_DATA_FIELDS", DefaultLanguageHighlighterColors.STATIC_FIELD);
    public static final TextAttributesKey DATA_ASSIGNED_FIELD =
            createTextAttributesKey("DQL_DATA_ASSIGNED_FIELDS", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey ENUM_VALUE =
            createTextAttributesKey("DQL_ENUM_VALUES", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey VARIABLE =
            createTextAttributesKey("DQL_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey PARENTHESES =
            createTextAttributesKey("DQL_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey BRACES =
            createTextAttributesKey("DQL_BRACES", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey BRACKETS =
            createTextAttributesKey("DQL_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey COMMA =
            createTextAttributesKey("DQL_COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey COLON =
            createTextAttributesKey("DQL_COLON", DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey SET =
            createTextAttributesKey("DQL_SET", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey DOT =
            createTextAttributesKey("DQL_DOT", DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey DURATION =
            createTextAttributesKey("DQL_DURATION", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey NULL =
            createTextAttributesKey("DQL_NULL", DefaultLanguageHighlighterColors.METADATA);
}
