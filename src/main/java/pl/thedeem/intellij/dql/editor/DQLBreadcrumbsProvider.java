package pl.thedeem.intellij.dql.editor;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLQuery;

import java.util.List;

public final class DQLBreadcrumbsProvider implements BreadcrumbsProvider {
    private static final Language[] LANGUAGES = {DynatraceQueryLanguage.INSTANCE};

    @Override
    public Language[] getLanguages() {
        return LANGUAGES;
    }

    @Override
    public boolean acceptElement(@NotNull PsiElement element) {
        if (element instanceof DQLQuery query) {
            return !query.getCommandList().isEmpty();
        }
        return false;
    }

    @Override
    public @NotNull String getElementInfo(@NotNull PsiElement element) {
        if (element instanceof DQLQuery query) {
            List<DQLCommand> commands = query.getCommandList();
            if (commands.isEmpty()) {
                return "";
            }
            String name = commands.getFirst().getName();
            return name != null ? name : "";
        }
        return "";
    }
}
