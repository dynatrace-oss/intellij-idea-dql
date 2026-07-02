package pl.thedeem.intellij.dql.services.variables;

import com.intellij.ide.IconProvider;
import com.intellij.json.psi.JsonFile;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLIcon;

import javax.swing.*;

public final class DQLVariablesIconProvider extends IconProvider implements DumbAware {
    @Override
    public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
        if (element instanceof JsonFile jsonFile) {
            VirtualFile virtualFile = jsonFile.getVirtualFile();
            if (virtualFile != null && DQLVariablesService.DQL_VARIABLES_FILE.equals(virtualFile.getName())) {
                return DQLIcon.DQL_VARIABLES_FILE;
            }
        }
        return null;
    }
}
