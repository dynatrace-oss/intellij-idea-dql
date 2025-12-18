package pl.thedeem.intellij.dql.psi;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;
import java.util.Map;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;

%%

%{
    public final static Map<String, IElementType> KEYWORDS = Map.of(
      "not", DQLTypes.NOT,
      "and", DQLTypes.AND,
      "or", DQLTypes.OR,
      "xor", DQLTypes.XOR,
      "true", DQLTypes.TRUE,
      "false", DQLTypes.FALSE,
      "null", DQLTypes.NULL_TYPE
    );
    public _DQLLexer() {
      this((java.io.Reader)null);
    }

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
      String currentIdentifier = (yycharat(-1) + yytext().toString()).toLowerCase().trim();
      return KEYWORDS.getOrDefault(currentIdentifier, DQLTypes.IDENTIFIER);
    }
%}

%public
%class _DQLLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%ignorecase
%caseless

%state IN_STRING
%state IN_ML_STRING
%state IN_RAW_STRING
%state IN_SQ_STRING
%state IN_IDENTIFIER
%state IN_ML_COMMENT

WHITE_SPACE=\s+

IDENTIFIER_START=[a-zA-Z_]
IDENTIFIER_EXTENSION=[a-zA-Z0-9_]+
IDENTIFIER=[a-zA-Z_][a-zA-Z0-9_]*
NUMBERED_IDENTIFIER=[0-9]+[a-zA-Z_]+
POSITIVE_LONG=[0-9]+
POSITIVE_DOUBLE=[0-9]+\.[0-9]+
POSITIVE_SCIENTIFIC_NOTATION=[0-9]+(\.[0-9]+)?e-?[0-9]+
EOL_COMMENT="//".*
MULTILINE_STRING_QUOTE="\"\"\""
DOUBLE_QUOTE="\""
SINGLE_QUOTE="'"
TICK_QUOTE="`"
ML_COMMENT_START="/*"
ML_COMMENT_FINISH="*/"

%%
<YYINITIAL> {
  {WHITE_SPACE}                    { return WHITE_SPACE; }
  "|"                              { return DQLTypes.PIPE; }
  "=="                             { return DQLTypes.EQUALS; }
  "!="                             { return DQLTypes.NOT_EQUALS; }
  "<>"                             { return DQLTypes.NOT_EQUALS_LG; }
  ">="                             { return DQLTypes.GREATER_OR_EQUALS_THAN; }
  "<="                             { return DQLTypes.LESSER_OR_EQUALS_THAN; }
  ">"                              { return DQLTypes.GREATER_THAN; }
  "<"                              { return DQLTypes.LESSER_THAN; }
  "{"                              { return DQLTypes.L_BRACE; }
  "}"                              { return DQLTypes.R_BRACE; }
  "("                              { return DQLTypes.L_PARENTHESIS; }
  ")"                              { return DQLTypes.R_PARENTHESIS; }
  "="                              { return DQLTypes.SET; }
  ","                              { return DQLTypes.COMMA; }
  ":"                              { return DQLTypes.COLON; }
  "."                              { return DQLTypes.DOT; }
  "["                              { return DQLTypes.ARRAY_OPEN; }
  "]"                              { return DQLTypes.ARRAY_CLOSE; }
  "+"                              { return DQLTypes.ADD; }
  "-"                              { return DQLTypes.SUBTRACT; }
  "*"                              { return DQLTypes.MULTIPLY; }
  "/"                              { return DQLTypes.DIVIDE; }
  "%"                              { return DQLTypes.MODULO; }
  "@"                              { return DQLTypes.AT; }
  "~"                              { return DQLTypes.SEARCH; }
  "$"{IDENTIFIER}                  { return DQLTypes.VARIABLE; }
  {POSITIVE_SCIENTIFIC_NOTATION}   { return DQLTypes.POSITIVE_SCIENTIFIC_NOTATION; }
  {NUMBERED_IDENTIFIER}            { return DQLTypes.NUMBERED_IDENTIFIER; }
  {POSITIVE_LONG}                  { return DQLTypes.POSITIVE_LONG; }
  {POSITIVE_DOUBLE}                { return DQLTypes.POSITIVE_DOUBLE; }
  {EOL_COMMENT}                    { return DQLTypes.EOL_COMMENT; }

  {IDENTIFIER_START} {
    yybegin(IN_IDENTIFIER);
  }
  {MULTILINE_STRING_QUOTE} {
    yybegin(IN_ML_STRING);
    return DQLTypes.MULTILINE_STRING_QUOTE;
  }
  {DOUBLE_QUOTE} {
    yybegin(IN_STRING);
    return DQLTypes.DOUBLE_QUOTE;
  }
  {TICK_QUOTE} {
    yybegin(IN_RAW_STRING);
    return DQLTypes.TICK_QUOTE;
  }
  {SINGLE_QUOTE} {
    yybegin(IN_SQ_STRING);
    return DQLTypes.SINGLE_QUOTE;
  }
  {ML_COMMENT_START} {
     yybegin(IN_ML_COMMENT);
  }
}

<IN_IDENTIFIER> {
    {IDENTIFIER_EXTENSION}?{WHITE_SPACE}?":" {
        yybegin(YYINITIAL);
        yypushback(calculatePushback(1));
        return DQLTypes.PARAM_IDENTIFIER;
    }
    {IDENTIFIER_EXTENSION}?("."{IDENTIFIER_EXTENSION})+ {
        yybegin(YYINITIAL);
        return DQLTypes.DOTTED_IDENTIFIER;
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

<IN_STRING> {
    {DOUBLE_QUOTE} {
        yybegin(YYINITIAL);
        return DQLTypes.DOUBLE_QUOTE;
    }
    "\\." {
    }
    ([^\"\\]+|\\.)+ {
        return DQLTypes.STRING_CONTENT;
    }
    <<EOF>> {
        yybegin(YYINITIAL);
        return TokenType.ERROR_ELEMENT;
    }
}

<IN_ML_STRING> {
    "\\\""/{MULTILINE_STRING_QUOTE} {
          return DQLTypes.STRING_CONTENT;
    }
    {MULTILINE_STRING_QUOTE} {
        yybegin(YYINITIAL);
        return DQLTypes.MULTILINE_STRING_QUOTE;
    }
    [^]/{MULTILINE_STRING_QUOTE} {
       return DQLTypes.STRING_CONTENT;
    }
    [^] {
    }
    <<EOF>> {
        yybegin(YYINITIAL);
        return TokenType.ERROR_ELEMENT;
    }
}

<IN_RAW_STRING> {
    {TICK_QUOTE} {
        yybegin(YYINITIAL);
        return DQLTypes.TICK_QUOTE;
    }
    "\\." {
    }
    ([^\`\\]+|\\.)+ {
        return DQLTypes.STRING_CONTENT;
    }
    <<EOF>> {
        yybegin(YYINITIAL);
        return TokenType.ERROR_ELEMENT;
    }
}

<IN_SQ_STRING> {
    {SINGLE_QUOTE} {
        yybegin(YYINITIAL);
        return DQLTypes.SINGLE_QUOTE;
    }
    "\\." {
    }
    ([^\'\\]+|\\.)+ {
        return DQLTypes.STRING_CONTENT;
    }
    <<EOF>> {
        yybegin(YYINITIAL);
        return TokenType.ERROR_ELEMENT;
    }
}

<IN_ML_COMMENT> {
    "\\\""/{ML_COMMENT_FINISH} {

    }
    {ML_COMMENT_FINISH} {
        yybegin(YYINITIAL);
        return DQLTypes.ML_COMMENT;
    }
    [^]/{ML_COMMENT_FINISH} {
    }
    [^] {
    }
    <<EOF>> {
        yybegin(YYINITIAL);
        return TokenType.ERROR_ELEMENT;
    }
}

[^] { return BAD_CHARACTER; }
