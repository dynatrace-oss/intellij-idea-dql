package pl.thedeem.intellij.dpl;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DPLFileType extends LanguageFileType {
    public static final DPLFileType INSTANCE = new DPLFileType();
    public static final String DPL_FILE_EXTENSION = ".dpl";
    public static final String DPL_FILE_DESCRIPTION = "Dynatrace Pattern Language file";

    private DPLFileType() {
        super(DynatracePatternLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return DynatracePatternLanguage.DPL_ID;
    }

    @NotNull
    @Override
    public String getDescription() {
        return DPL_FILE_DESCRIPTION;
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DPL_FILE_EXTENSION;
    }

    @Override
    public Icon getIcon() {
        return DPLIcon.DYNATRACE_LOGO;
    }
}
