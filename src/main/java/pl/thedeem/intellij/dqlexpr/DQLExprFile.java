package pl.thedeem.intellij.dqlexpr;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class DQLExprFile extends PsiFileBase {
    public DQLExprFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, DQLExprLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return DQLExprFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "DQL Expression File";
    }
}
