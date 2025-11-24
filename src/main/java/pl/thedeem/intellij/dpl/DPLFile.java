package pl.thedeem.intellij.dpl;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class DPLFile extends PsiFileBase {
    public DPLFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, DynatracePatternLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return DPLFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "DPL File";
    }
}
