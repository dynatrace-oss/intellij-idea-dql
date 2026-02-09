package pl.thedeem.intellij.dqlpart;

import com.intellij.lang.Language;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;

public class DQLPartLanguage extends Language {
    public static final DQLPartLanguage INSTANCE = new DQLPartLanguage();

    public static final String DQL_PART_ID = "DQLPart";

    private DQLPartLanguage() {
        super(DynatraceQueryLanguage.INSTANCE, DQL_PART_ID);
    }
}
