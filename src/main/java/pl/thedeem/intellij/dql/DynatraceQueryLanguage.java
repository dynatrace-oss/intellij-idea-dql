package pl.thedeem.intellij.dql;

import com.intellij.lang.Language;

public class DynatraceQueryLanguage extends Language {
    public static final DynatraceQueryLanguage INSTANCE = new DynatraceQueryLanguage();
    public static final String DQL_ID = "DQL";
    public static final String DQL_DISPLAY_NAME = "Dynatrace Query Language";

    private DynatraceQueryLanguage() {
        super(DQL_ID);
    }
}
