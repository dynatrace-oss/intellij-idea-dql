package pl.thedeem.intellij.dql.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import pl.thedeem.intellij.dql.DQLLexerAdapter;
import pl.thedeem.intellij.dql.psi.DQLTokenSets;
import pl.thedeem.intellij.dql.psi.DQLTypes;
import org.jetbrains.annotations.NotNull;

public class DQLSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{DQLColorScheme.BAD_CHARACTER};
    private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{DQLColorScheme.COMMENT};
    private static final TextAttributesKey[] OPERATOR_KEYS = new TextAttributesKey[]{DQLColorScheme.OPERATOR};
    private static final TextAttributesKey[] STRING_LITERAL_KEYS = new TextAttributesKey[]{DQLColorScheme.STRING};
    private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{DQLColorScheme.NUMBER};
    private static final TextAttributesKey[] BOOLEAN_KEYS = new TextAttributesKey[]{DQLColorScheme.BOOLEAN};
    private static final TextAttributesKey[] IDENTIFIER_KEYS = new TextAttributesKey[]{DQLColorScheme.IDENTIFIER};
    private static final TextAttributesKey[] VARIABLE_KEYS = new TextAttributesKey[]{DQLColorScheme.VARIABLE};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    private static final TextAttributesKey[] PARENTHESES_KEYS = new TextAttributesKey[]{DQLColorScheme.PARENTHESES};
    private static final TextAttributesKey[] BRACES_KEYS = new TextAttributesKey[]{DQLColorScheme.BRACES};
    private static final TextAttributesKey[] BRACKETS_KEYS = new TextAttributesKey[]{DQLColorScheme.BRACKETS};
    private static final TextAttributesKey[] COMMA_KEYS = new TextAttributesKey[]{DQLColorScheme.COMMA};
    private static final TextAttributesKey[] COLON_KEYS = new TextAttributesKey[]{DQLColorScheme.COLON};
    private static final TextAttributesKey[] SET_KEYS = new TextAttributesKey[]{DQLColorScheme.SET};
    private static final TextAttributesKey[] DOT_KEYS = new TextAttributesKey[]{DQLColorScheme.DOT};
    private static final TextAttributesKey[] CONDITION_KEYS = new TextAttributesKey[]{DQLColorScheme.KEYWORD};
    private static final TextAttributesKey[] PARAMS_KEYS = new TextAttributesKey[]{DQLColorScheme.PARAMETER};
    private static final TextAttributesKey[] DURATION_KEYS = new TextAttributesKey[]{DQLColorScheme.DURATION};
    private static final TextAttributesKey[] NULL_KEYS = new TextAttributesKey[]{DQLColorScheme.NULL};

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new DQLLexerAdapter();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (DQLTokenSets.CONDITION_OPERANDS.contains(tokenType)) {
            return CONDITION_KEYS;
        }
        if (DQLTokenSets.ALL_OPERATORS.contains(tokenType)) {
            return OPERATOR_KEYS;
        }
        if (DQLTokenSets.COMMENTS.contains(tokenType)) {
            return COMMENT_KEYS;
        }
        if (DQLTokenSets.STRING_LITERALS.contains(tokenType) || DQLTokenSets.STRING_QUOTES.contains(tokenType)) {
            return STRING_LITERAL_KEYS;
        }
        if (DQLTokenSets.NUMBERS.contains(tokenType)) {
            return NUMBER_KEYS;
        }
        if (DQLTokenSets.DURATIONS.contains(tokenType)) {
            return DURATION_KEYS;
        }
        if (DQLTokenSets.BOOLEAN.contains(tokenType)) {
            return BOOLEAN_KEYS;
        }
        if (tokenType.equals(DQLTypes.NULL_TYPE)) {
            return NULL_KEYS;
        }
        if (tokenType.equals(DQLTypes.VARIABLE)) {
            return VARIABLE_KEYS;
        }
        if (tokenType.equals(DQLTypes.PARAM_IDENTIFIER)) {
            return PARAMS_KEYS;
        }
        if (DQLTokenSets.IDENTIFIERS.contains(tokenType)) {
            return IDENTIFIER_KEYS;
        }
        if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
        }
        if (DQLTokenSets.PARENTHESES.contains(tokenType)) {
            return PARENTHESES_KEYS;
        }
        if (DQLTokenSets.BRACKETS.contains(tokenType)) {
            return BRACKETS_KEYS;
        }
        if (DQLTokenSets.BRACES.contains(tokenType)) {
            return BRACES_KEYS;
        }
        if (DQLTypes.COMMA.equals(tokenType)) {
            return COMMA_KEYS;
        }
        if (DQLTypes.SET.equals(tokenType)) {
            return SET_KEYS;
        }
        if (DQLTypes.DOT.equals(tokenType)) {
            return DOT_KEYS;
        }
        if (DQLTypes.COLON.equals(tokenType)) {
            return COLON_KEYS;
        }
        return EMPTY_KEYS;
    }
}
