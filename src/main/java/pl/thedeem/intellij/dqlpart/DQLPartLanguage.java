package pl.thedeem.intellij.dqlpart;

import com.intellij.lang.Language;

public class DQLPartLanguage extends Language {
    public static final DQLPartLanguage INSTANCE = new DQLPartLanguage();

    public static final String DQL_PART_ID = "DQLPart";

    private DQLPartLanguage() {
        super(DQL_PART_ID);
    }
}
