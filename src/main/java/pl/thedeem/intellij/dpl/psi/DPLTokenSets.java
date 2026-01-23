package pl.thedeem.intellij.dpl.psi;

import com.intellij.psi.tree.TokenSet;

public interface DPLTokenSets {
    TokenSet STRING_LITERALS = TokenSet.create(DPLTypes.STRING_CONTENT);
    TokenSet STRING_QUOTES = TokenSet.create(DPLTypes.SINGLE_QUOTE, DPLTypes.DOUBLE_QUOTE);
    TokenSet NUMBERS = TokenSet.create(DPLTypes.LONG, DPLTypes.DOUBLE, DPLTypes.SI_NUMBER, DPLTypes.HEX_NUMBER);
    TokenSet BOOLEAN = TokenSet.create(DPLTypes.TRUE, DPLTypes.FALSE);
    TokenSet BRACKETS = TokenSet.create(DPLTypes.L_BRACKET, DPLTypes.R_BRACKET);
    TokenSet PARENTHESES = TokenSet.create(DPLTypes.L_PAREN, DPLTypes.R_PAREN);
    TokenSet DIAMOND = TokenSet.create(DPLTypes.L_ARROW, DPLTypes.R_ARROW);
    TokenSet BRACES = TokenSet.create(DPLTypes.L_BRACE, DPLTypes.R_BRACE);
    TokenSet LOOKAROUND = TokenSet.create(DPLTypes.PLA, DPLTypes.PLB);
    TokenSet QUANTIFIERS = TokenSet.create(DPLTypes.OPTIONAL, DPLTypes.MULTIPLY, DPLTypes.ADD);
    TokenSet IDENTIFIERS = TokenSet.create(
            DPLTypes.IDENTIFIER, DPLTypes.FIELD_NAME, DPLTypes.PARAMETER_NAME, DPLTypes.MATCHER_NAME, DPLTypes.VARIABLE_NAME);
    TokenSet COMMENTS = TokenSet.create(DPLTypes.EOL_COMMENT, DPLTypes.ML_COMMENT);
    TokenSet EXPRESSION_PARTS = TokenSet.orSet(
            QUANTIFIERS,
            DIAMOND,
            PARENTHESES,
            BRACES,
            BRACKETS,
            TokenSet.create(DPLTypes.COLON, DPLTypes.OR)
    );
}
