package pl.thedeem.intellij.dpl;

import com.intellij.lang.Language;

public class DynatracePatternLanguage extends Language {
    public static final DynatracePatternLanguage INSTANCE = new DynatracePatternLanguage();
    public static final String DPL_ID = "DPL";
    public static final String DPL_DISPLAY_NAME = "Dynatrace Pattern Language";

    private DynatracePatternLanguage() {
        super(DPL_ID);
    }
}
