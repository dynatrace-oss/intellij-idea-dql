package pl.thedeem.intellij.edql;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class EDQLFile extends PsiFileBase {
    public EDQLFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, EDQLLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return EDQLFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "EDQL File";
    }
}
