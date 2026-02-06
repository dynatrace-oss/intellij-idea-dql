package pl.thedeem.intellij.edql;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.Icons;

import javax.swing.*;

public class EDQLFileType extends LanguageFileType {
    public static final EDQLFileType INSTANCE = new EDQLFileType();

    public static final String EDQL_FILE_EXTENSION = "edql";
    public static final String EDQL_FILE_DESCRIPTION = "Dynatrace Expression Query Language file";

    private EDQLFileType() {
        super(EDQLLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return EDQLLanguage.EDQL_ID;
    }

    @Override
    public @NotNull String getDescription() {
        return EDQL_FILE_DESCRIPTION;
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return EDQL_FILE_EXTENSION;
    }

    @Override
    public Icon getIcon() {
        return Icons.DYNATRACE_LOGO;
    }
}
