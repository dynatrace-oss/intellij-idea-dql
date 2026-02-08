package pl.thedeem.intellij.dqlpart;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.Icons;

import javax.swing.*;

public class DQLPartFileType extends LanguageFileType {
    public static final DQLPartFileType INSTANCE = new DQLPartFileType();

    public static final String DQL_PART_FILE_EXTENSION = "dqlpart";
    public static final String DQL_PART_FILE_DESCRIPTION = "Dynatrace Query Language part file";

    private DQLPartFileType() {
        super(DQLPartLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return DQLPartLanguage.DQL_PART_ID;
    }

    @Override
    public @NotNull String getDescription() {
        return DQL_PART_FILE_DESCRIPTION;
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return DQL_PART_FILE_EXTENSION;
    }

    @Override
    public Icon getIcon() {
        return Icons.DYNATRACE_LOGO;
    }
}
