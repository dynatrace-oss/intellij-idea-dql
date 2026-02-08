package pl.thedeem.intellij.dqlexpr;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.Icons;

import javax.swing.*;

public class DQLExprFileType extends LanguageFileType {
    public static final DQLExprFileType INSTANCE = new DQLExprFileType();

    public static final String DQL_EXPR_FILE_EXTENSION = "dqlexpr";
    public static final String DQL_EXPR_FILE_DESCRIPTION = "Dynatrace Query Language Expression file";

    private DQLExprFileType() {
        super(DQLExprLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return DQLExprLanguage.DQL_EXPR_ID;
    }

    @Override
    public @NotNull String getDescription() {
        return DQL_EXPR_FILE_DESCRIPTION;
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return DQL_EXPR_FILE_EXTENSION;
    }

    @Override
    public Icon getIcon() {
        return Icons.DYNATRACE_LOGO;
    }
}
