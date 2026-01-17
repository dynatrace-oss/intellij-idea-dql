package pl.thedeem.intellij.dpl.psi;

import java.util.Map;
import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.psi.TokenType.ERROR_ELEMENT;

%%

%{
    public final static Map<String, IElementType> KEYWORDS = Map.of(
      "true", DPLTypes.TRUE,
      "false", DPLTypes.FALSE,
      "null", DPLTypes.NULL_TYPE
    );

    public int calculatePushback(int initial) {
      int len = yylength();
      int wsLen = initial;
      // Count whitespace characters
      for (int i = len - 2; i >= 0; i--) {
          char c = yytext().charAt(i);
          if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
              wsLen++;
          } else {
              break;
          }
      }
      return wsLen;
    }

    public IElementType getIdentifierToken() {
      String currentIdentifier = (yycharat(-1) + yytext().toString()).trim().toLowerCase();
      return KEYWORDS.getOrDefault(currentIdentifier, DPLTypes.IDENTIFIER);
    }
%}

%public
%class _DPLLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%ignorecase
%caseless

%state IN_STRING
%state IN_SQ_STRING
%state IN_IDENTIFIER
%state IN_CHARACTER_CLASS
%state IN_ML_COMMENT

WHITE_SPACE=\s+
DOUBLE_QUOTE="\""
SINGLE_QUOTE="'"
IDENTIFIER_START=[A-Za-z_]
IDENTIFIER_EXTENSION=[A-Za-z0-9_]+
LONG=-?[0-9]+
DOUBLE=-?[0-9]+\.[0-9]+
SCIENTIFIC_NOTATION=-?[0-9]+(\.[0-9]+)?e-?[0-9]+
HEX_NOTATION=-?0[xX][0-9A-Fa-f]+
EOL_COMMENT="//".*
ML_COMMENT_START="/*"
ML_COMMENT_FINISH="*/"

%%

<YYINITIAL> {
  {WHITE_SPACE}                    { return WHITE_SPACE; }
  "?"                              { return DPLTypes.OPTIONAL; }
  "{"                              { return DPLTypes.L_BRACE; }
  "}"                              { return DPLTypes.R_BRACE; }
  "("                              { return DPLTypes.L_PAREN; }
  ")"                              { return DPLTypes.R_PAREN; }
  "="                              { return DPLTypes.SET; }
  ","                              { return DPLTypes.COMMA; }
  ":"                              { return DPLTypes.COLON; }
  "-"                              { return DPLTypes.SUBTRACT; }
  "+"                              { return DPLTypes.ADD; }
  "*"                              { return DPLTypes.MULTIPLY; }
  ">>"                             { return DPLTypes.PLA; }
  "<<"                             { return DPLTypes.PLB; }
  "|"                              { return DPLTypes.OR; }
  ";"                              { return DPLTypes.SEMICOLON; }
  "!"                              { return DPLTypes.NEGATION; }
  "<"                              { return DPLTypes.L_ARROW; }
  ">"                              { return DPLTypes.R_ARROW; }
  {SCIENTIFIC_NOTATION}            { return DPLTypes.SI_NUMBER; }
  {HEX_NOTATION}                   { return DPLTypes.HEX_NUMBER; }
  {LONG}                           { return DPLTypes.LONG; }
  {DOUBLE}                         { return DPLTypes.DOUBLE; }
  "$"{IDENTIFIER_START}{IDENTIFIER_EXTENSION}? {
    return DPLTypes.VARIABLE_NAME;
  }
  {EOL_COMMENT}                    { return DPLTypes.EOL_COMMENT; }
  {IDENTIFIER_START} {
    yybegin(IN_IDENTIFIER);
  }
  {DOUBLE_QUOTE} {
    yybegin(IN_STRING);
    return DPLTypes.DOUBLE_QUOTE;
  }
  {SINGLE_QUOTE} {
    yybegin(IN_SQ_STRING);
    return DPLTypes.SINGLE_QUOTE;
  }
  "[" {
      yybegin(IN_CHARACTER_CLASS);
      return DPLTypes.L_BRACKET;
  }
  {ML_COMMENT_START} {
   yybegin(IN_ML_COMMENT);
  }
}

<IN_STRING> {
    {DOUBLE_QUOTE} {
        yybegin(YYINITIAL);
        return DPLTypes.DOUBLE_QUOTE;
    }
    "\\." {
    }
    ([^\"\\]+|\\.)+ {
        return DPLTypes.STRING_CONTENT;
    }
    <<EOF>> {
        yybegin(YYINITIAL);
        return ERROR_ELEMENT;
    }
}

<IN_SQ_STRING> {
    {SINGLE_QUOTE} {
        yybegin(YYINITIAL);
        return DPLTypes.SINGLE_QUOTE;
    }
    "\\." {
    }
    ([^'\\]+|\\.)+ {
        return DPLTypes.STRING_CONTENT;
    }
    <<EOF>> {
        yybegin(YYINITIAL);
        return ERROR_ELEMENT;
    }
}

<IN_CHARACTER_CLASS> {
    "]" {
        yybegin(YYINITIAL);
        return DPLTypes.R_BRACKET;
    }
    "\\." {
    }
    ([^\]\\]+|\\.)+ {
        return DPLTypes.CHARACTER_CLASS;
    }
    <<EOF>> {
        yybegin(YYINITIAL);
        return ERROR_ELEMENT;
    }
}

<IN_IDENTIFIER> {
    {IDENTIFIER_EXTENSION}?("."{IDENTIFIER_EXTENSION})+ {
        yybegin(YYINITIAL);
        return DPLTypes.DOTTED_IDENTIFIER;
    }
    {IDENTIFIER_EXTENSION} {
        IElementType result = getIdentifierToken();
        yybegin(YYINITIAL);
        return result;
    }
    {WHITE_SPACE} {
        yypushback(calculatePushback(1));
        IElementType result = getIdentifierToken();
        yybegin(YYINITIAL);
        return result;
    }
    . {
        yypushback(1);
        IElementType result = getIdentifierToken();
        yybegin(YYINITIAL);
        return result;
    }
    <<EOF>> {
        IElementType result = getIdentifierToken();
        yybegin(YYINITIAL);
        return result;
    }
}

<IN_ML_COMMENT> {
    "\\\""/{ML_COMMENT_FINISH} {

    }
    {ML_COMMENT_FINISH} {
        yybegin(YYINITIAL);
        return DPLTypes.ML_COMMENT;
    }
    [^]/{ML_COMMENT_FINISH} {
    }
    [^] {
    }
    <<EOF>> {
        yybegin(YYINITIAL);
        return ERROR_ELEMENT;
    }
}

[^] { return BAD_CHARACTER; }