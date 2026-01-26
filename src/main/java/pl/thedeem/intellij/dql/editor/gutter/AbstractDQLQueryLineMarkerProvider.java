package pl.thedeem.intellij.dql.editor.gutter;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLCommandKeyword;
import pl.thedeem.intellij.dql.psi.DQLQuery;

public abstract class AbstractDQLQueryLineMarkerProvider extends LineMarkerProviderDescriptor implements LineMarkerProvider, DumbAware {
    @Override
    public final @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        // LineMarkerProvider is supposed to be registered for leaf elements only.
        if (element.getFirstChild() != null) {
            return null;
        }

        if (!(element.getParent() instanceof DQLCommandKeyword keyword
                && keyword.getParent() instanceof DQLCommand command
                && command.isFirstStatement())
        ) {
            return null;
        }

        DQLQuery query = PsiTreeUtil.getParentOfType(command, DQLQuery.class);
        if (query == null || !isEnabled(element, query)) {
            return null;
        }
        return getLineMarkerInfo(element, query);
    }

    protected boolean isDqlFile(@NotNull PsiElement element) {
        PsiFile topLevelFile = InjectedLanguageManager.getInstance(element.getProject()).getTopLevelFile(element);
        return DQLFileType.INSTANCE.equals(topLevelFile.getVirtualFile().getFileType());
    }

    protected abstract boolean isEnabled(@NotNull PsiElement element, @NotNull DQLQuery query);

    protected abstract @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element, @NotNull DQLQuery query);
}
