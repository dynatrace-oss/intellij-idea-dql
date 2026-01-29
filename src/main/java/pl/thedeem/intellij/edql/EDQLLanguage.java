package pl.thedeem.intellij.edql;

import com.intellij.lang.Language;

public class EDQLLanguage extends Language {
    public static final EDQLLanguage INSTANCE = new EDQLLanguage();

    public static final String EDQL_ID = "EDQL";

    private EDQLLanguage() {
        super(EDQL_ID);
    }
}
