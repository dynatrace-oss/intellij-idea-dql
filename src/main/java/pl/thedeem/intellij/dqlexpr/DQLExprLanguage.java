package pl.thedeem.intellij.dqlexpr;

import com.intellij.lang.Language;

public class DQLExprLanguage extends Language {
    public static final DQLExprLanguage INSTANCE = new DQLExprLanguage();

    public static final String DQL_EXPR_ID = "DQLExpr";

    private DQLExprLanguage() {
        super(DQL_EXPR_ID);
    }
}
