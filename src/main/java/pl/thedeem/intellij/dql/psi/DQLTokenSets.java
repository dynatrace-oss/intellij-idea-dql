package pl.thedeem.intellij.dql.psi;

import com.intellij.psi.tree.TokenSet;

public interface DQLTokenSets {
    TokenSet COMMENTS = TokenSet.create(DQLTypes.EOL_COMMENT, DQLTypes.ML_COMMENT);
    TokenSet ALL_OPERATORS = TokenSet.create(DQLTypes.EQUALS, DQLTypes.NOT_EQUALS, DQLTypes.NOT_EQUALS_LG, DQLTypes.GREATER_THAN,
            DQLTypes.GREATER_OR_EQUALS_THAN, DQLTypes.LESSER_THAN, DQLTypes.LESSER_OR_EQUALS_THAN, DQLTypes.PIPE,
            DQLTypes.ADD, DQLTypes.SUBTRACT, DQLTypes.MULTIPLY, DQLTypes.DIVIDE, DQLTypes.MODULO, DQLTypes.AT);
    TokenSet WORD_EXPRESSION_OPERATORS = TokenSet.create(
            DQLTypes.CONDITION_OPERATOR,
            DQLTypes.NOT,
            DQLTypes.SORT_DIRECTION
    );
    TokenSet CONDITION_OPERANDS = TokenSet.create(DQLTypes.AND, DQLTypes.OR, DQLTypes.XOR, DQLTypes.NOT);
    TokenSet STRING_LITERALS = TokenSet.create(DQLTypes.STRING_CONTENT);
    TokenSet STRING_QUOTES = TokenSet.create(DQLTypes.DOUBLE_QUOTE, DQLTypes.SINGLE_QUOTE, DQLTypes.MULTILINE_STRING_QUOTE, DQLTypes.TICK_QUOTE);
    TokenSet NUMBERS = TokenSet.create(DQLTypes.POSITIVE_DOUBLE, DQLTypes.POSITIVE_LONG, DQLTypes.NUMBER, DQLTypes.POSITIVE_SCIENTIFIC_NOTATION);
    TokenSet DURATIONS = TokenSet.create(DQLTypes.DURATION);
    TokenSet BOOLEAN = TokenSet.create(DQLTypes.BOOLEAN, DQLTypes.TRUE, DQLTypes.FALSE);
    TokenSet PARENTHESES = TokenSet.create(DQLTypes.L_PARENTHESIS, DQLTypes.R_PARENTHESIS);
    TokenSet BRACKETS = TokenSet.create(DQLTypes.ARRAY_OPEN, DQLTypes.ARRAY_CLOSE);
    TokenSet BRACES = TokenSet.create(DQLTypes.L_BRACE, DQLTypes.R_BRACE);
    TokenSet IDENTIFIERS = TokenSet.create(
            DQLTypes.IDENTIFIER,
            DQLTypes.DOTTED_IDENTIFIER,
            DQLTypes.PARAM_IDENTIFIER,
            DQLTypes.VARIABLE
    );
}
