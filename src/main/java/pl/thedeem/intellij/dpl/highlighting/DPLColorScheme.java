package pl.thedeem.intellij.dpl.highlighting;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public interface DPLColorScheme {
    TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("DPL_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    TextAttributesKey STRING =
            createTextAttributesKey("DPL_STRING", DefaultLanguageHighlighterColors.STRING);
    TextAttributesKey NUMBER =
            createTextAttributesKey("DPL_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    TextAttributesKey BOOLEAN =
            createTextAttributesKey("DPL_BOOLEAN", DefaultLanguageHighlighterColors.CONSTANT);
    TextAttributesKey IDENTIFIER =
            createTextAttributesKey("DPL_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    TextAttributesKey MACRO =
            createTextAttributesKey("DPL_MACRO", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    TextAttributesKey PARENTHESES =
            createTextAttributesKey("DPL_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    TextAttributesKey DIAMOND =
            createTextAttributesKey("DPL_DIAMOND", DefaultLanguageHighlighterColors.BRACKETS);
    TextAttributesKey BRACES =
            createTextAttributesKey("DPL_BRACES", DefaultLanguageHighlighterColors.BRACES);
    TextAttributesKey BRACKETS =
            createTextAttributesKey("DPL_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    TextAttributesKey COMMA =
            createTextAttributesKey("DPL_COMMA", DefaultLanguageHighlighterColors.COMMA);
    TextAttributesKey COLON =
            createTextAttributesKey("DPL_COLON", DefaultLanguageHighlighterColors.SEMICOLON);
    TextAttributesKey SEMICOLON =
            createTextAttributesKey("DPL_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
    TextAttributesKey LOOKAROUND =
            createTextAttributesKey("DPL_LOOKAROUND", DefaultLanguageHighlighterColors.MARKUP_TAG);
    TextAttributesKey QUANTIFIERS =
            createTextAttributesKey("DPL_QUANTIFIERS", DefaultLanguageHighlighterColors.MARKUP_TAG);
    TextAttributesKey SET =
            createTextAttributesKey("DPL_SET", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    TextAttributesKey DURATION =
            createTextAttributesKey("DPL_DURATION", DefaultLanguageHighlighterColors.NUMBER);
    TextAttributesKey NULL =
            createTextAttributesKey("DPL_NULL", DefaultLanguageHighlighterColors.METADATA);
    TextAttributesKey NEGATION =
            createTextAttributesKey("DPL_NEGATION", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);
    TextAttributesKey KEYWORD =
            createTextAttributesKey("DPL_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    TextAttributesKey FIELD_NAME =
            createTextAttributesKey("DPL_FIELD_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    TextAttributesKey CONFIGURATION_NAME =
            createTextAttributesKey("DPL_CONFIGURATION_NAME", DefaultLanguageHighlighterColors.STATIC_METHOD);
    TextAttributesKey REGEX =
            createTextAttributesKey("DPL_REGEX", DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE);
    TextAttributesKey LINE_COMMENT =
            createTextAttributesKey("DPL_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    TextAttributesKey BLOCK_COMMENT =
            createTextAttributesKey("DPL_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
}
