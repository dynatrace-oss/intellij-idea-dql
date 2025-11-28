package pl.thedeem.intellij.dpl.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLLexerAdapter;
import pl.thedeem.intellij.dpl.psi.DPLTokenSets;
import pl.thedeem.intellij.dpl.psi.DPLTypes;

public class DPLSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{DPLColorScheme.BAD_CHARACTER};
    private static final TextAttributesKey[] STRING_LITERAL_KEYS = new TextAttributesKey[]{DPLColorScheme.STRING};
    private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{DPLColorScheme.NUMBER};
    private static final TextAttributesKey[] BOOLEAN_KEYS = new TextAttributesKey[]{DPLColorScheme.BOOLEAN};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    private static final TextAttributesKey[] PARENTHESES_KEYS = new TextAttributesKey[]{DPLColorScheme.PARENTHESES};
    private static final TextAttributesKey[] BRACES_KEYS = new TextAttributesKey[]{DPLColorScheme.BRACES};
    private static final TextAttributesKey[] BRACKETS_KEYS = new TextAttributesKey[]{DPLColorScheme.BRACKETS};
    private static final TextAttributesKey[] COMMA_KEYS = new TextAttributesKey[]{DPLColorScheme.COMMA};
    private static final TextAttributesKey[] COLON_KEYS = new TextAttributesKey[]{DPLColorScheme.COLON};
    private static final TextAttributesKey[] SEMICOLON_KEYS = new TextAttributesKey[]{DPLColorScheme.SEMICOLON};
    private static final TextAttributesKey[] LOOKAROUND_KEYS = new TextAttributesKey[]{DPLColorScheme.LOOKAROUND};
    private static final TextAttributesKey[] QUANTIFIERS_KEYS = new TextAttributesKey[]{DPLColorScheme.QUANTIFIERS};
    private static final TextAttributesKey[] SET_KEYS = new TextAttributesKey[]{DPLColorScheme.SET};
    private static final TextAttributesKey[] NULL_KEYS = new TextAttributesKey[]{DPLColorScheme.NULL};
    private static final TextAttributesKey[] NEGATION_KEYS = new TextAttributesKey[]{DPLColorScheme.NEGATION};
    private static final TextAttributesKey[] MACRO_KEYS = new TextAttributesKey[]{DPLColorScheme.MACRO};
    private static final TextAttributesKey[] REGEX_KEYS = new TextAttributesKey[]{DPLColorScheme.REGEX};

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new DPLLexerAdapter();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (DPLTokenSets.STRING_LITERALS.contains(tokenType) || DPLTokenSets.STRING_QUOTES.contains(tokenType)) {
            return STRING_LITERAL_KEYS;
        }
        if (DPLTokenSets.NUMBERS.contains(tokenType)) {
            return NUMBER_KEYS;
        }
        if (DPLTokenSets.BOOLEAN.contains(tokenType)) {
            return BOOLEAN_KEYS;
        }
        if (tokenType.equals(DPLTypes.NULL_TYPE)) {
            return NULL_KEYS;
        }
        if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
        }
        if (DPLTokenSets.PARENTHESES.contains(tokenType)) {
            return PARENTHESES_KEYS;
        }
        if (DPLTokenSets.BRACKETS.contains(tokenType)) {
            return BRACKETS_KEYS;
        }
        if (DPLTokenSets.BRACES.contains(tokenType)) {
            return BRACES_KEYS;
        }
        if (DPLTypes.COMMA.equals(tokenType)) {
            return COMMA_KEYS;
        }
        if (DPLTypes.SET.equals(tokenType)) {
            return SET_KEYS;
        }
        if (DPLTypes.COLON.equals(tokenType)) {
            return COLON_KEYS;
        }
        if (DPLTypes.SEMICOLON.equals(tokenType)) {
            return SEMICOLON_KEYS;
        }
        if (DPLTokenSets.LOOKAROUND.contains(tokenType)) {
            return LOOKAROUND_KEYS;
        }
        if (DPLTokenSets.QUANTIFIERS.contains(tokenType)) {
            return QUANTIFIERS_KEYS;
        }
        if (DPLTypes.NEGATION.equals(tokenType)) {
            return NEGATION_KEYS;
        }
        if (DPLTypes.VARIABLE_NAME.equals(tokenType)) {
            return MACRO_KEYS;
        }
        if (DPLTypes.CHARACTER_CLASS.equals(tokenType)) {
            return REGEX_KEYS;
        }

        return EMPTY_KEYS;
    }
}
