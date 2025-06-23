package pl.thedeem.intellij.dql;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class DQLFile extends PsiFileBase {
    public DQLFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, DynatraceQueryLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return DQLFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "DQL File";
    }
}
