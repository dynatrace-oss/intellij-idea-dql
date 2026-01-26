package pl.thedeem.intellij.dql.editor.gutter;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFile;
import pl.thedeem.intellij.dql.psi.DQLQuery;

public class DQLSubqueryExecutionLineMarkerProvider extends DQLFileQueryExecutionLineMarkerProvider {
    @Override
    public String getName() {
        return DQLBundle.message("gutter.executeDQL.injectedFragment.name");
    }

    @Override
    protected boolean isEnabled(@NotNull PsiElement element, @NotNull DQLQuery query) {
        return !(query.getParent() instanceof DQLFile);
    }

    @Override
    protected <T extends PsiElement> @Nullable GutterIconNavigationHandler<T> createNavigationHandler(
            @NotNull AnAction mainAction,
            @NotNull T element,
            @NotNull DQLQuery query
    ) {
        if (isDqlFile(element)) {
            return null;
        }
        return (mouseEvent, elt) -> {
            ApplicationManager.getApplication().invokeLater(() -> ActionManager.getInstance().tryToExecute(
                    mainAction,
                    null,
                    null,
                    ActionPlaces.UNKNOWN,
                    true
            ));
        };
    }

    @Override
    protected LineMarkerInfo.@Nullable LineMarkerGutterIconRenderer<PsiElement> createActionGutterRenderer(
            @NotNull LineMarkerInfo<PsiElement> lineMarkerInfo,
            @NotNull AnAction action,
            @NotNull PsiElement element) {
        if (!isDqlFile(element)) {
            return null;
        }
        return super.createActionGutterRenderer(lineMarkerInfo, action, element);
    }
}
