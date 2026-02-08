package pl.thedeem.intellij.dqlpart;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class DQLPartFile extends PsiFileBase {
    public DQLPartFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, DQLPartLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return DQLPartFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "DQL Part File";
    }
}
