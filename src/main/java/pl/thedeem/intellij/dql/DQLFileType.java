package pl.thedeem.intellij.dql;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DQLFileType extends LanguageFileType {
    public static final DQLFileType INSTANCE = new DQLFileType();
    public static final String DQL_FILE_EXTENSION = ".dql";
    public static final String DQL_FILE_DESCRIPTION = "Dynatrace Query Language file";

    private DQLFileType() {
        super(DynatraceQueryLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return DynatraceQueryLanguage.DQL_ID;
    }

    @NotNull
    @Override
    public String getDescription() {
        return DQL_FILE_DESCRIPTION;
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DQL_FILE_EXTENSION;
    }

    @Override
    public Icon getIcon() {
        return DQLIcon.DYNATRACE_LOGO;
    }
}
