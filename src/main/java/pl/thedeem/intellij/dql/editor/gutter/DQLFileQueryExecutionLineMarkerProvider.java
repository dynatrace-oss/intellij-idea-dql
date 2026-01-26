package pl.thedeem.intellij.dql.editor.gutter;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFile;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.editor.actions.ExecuteDQLQueryAction;
import pl.thedeem.intellij.dql.psi.DQLQuery;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import javax.swing.*;

public class DQLFileQueryExecutionLineMarkerProvider extends AbstractDQLQueryLineMarkerProvider {
    @Override
    public String getName() {
        return DQLBundle.message("gutter.executeDQL.wholeQuery.name");
    }

    @Override
    public @NotNull Icon getIcon() {
        return DQLIcon.GUTTER_EXECUTE_DQL;
    }

    @Override
    protected @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element, @NotNull DQLQuery query) {
        AnAction action = createAction(element, query);
        return new LineMarkerInfo<>(
                element,
                element.getTextRange(),
                getIcon(),
                it -> getName(),
                createNavigationHandler(action, element, query),
                GutterIconRenderer.Alignment.RIGHT,
                this::getName
        ) {
            @Override
            public GutterIconRenderer createGutterRenderer() {
                LineMarkerGutterIconRenderer<PsiElement> custom = createActionGutterRenderer(
                        this,
                        action,
                        element
                );
                return custom == null ? super.createGutterRenderer() : custom;
            }
        };
    }

    @Override
    protected boolean isEnabled(@NotNull PsiElement element, @NotNull DQLQuery query) {
        return query.getParent() instanceof DQLFile && isDqlFile(query);
    }

    protected @Nullable LineMarkerInfo.LineMarkerGutterIconRenderer<PsiElement> createActionGutterRenderer(
            @NotNull LineMarkerInfo<PsiElement> lineMarkerInfo,
            @NotNull AnAction action,
            @NotNull PsiElement element
    ) {
        return new LineMarkerInfo.LineMarkerGutterIconRenderer<>(lineMarkerInfo) {
            @Override
            public AnAction getClickAction() {
                return action;
            }

            @Override
            public boolean isNavigateAction() {
                return true;
            }
        };
    }

    protected <T extends PsiElement> @Nullable GutterIconNavigationHandler<T> createNavigationHandler(
            @NotNull AnAction mainAction,
            @NotNull T element,
            @NotNull DQLQuery query
    ) {
        return null;
    }

    protected @NotNull AnAction createAction(@NotNull PsiElement element, @NotNull DQLQuery query) {
        DQLQueryConfigurationService service = DQLQueryConfigurationService.getInstance();
        SmartPsiElementPointer<PsiElement> pointer = SmartPointerManager.createPointer(element);
        return new ExecuteDQLQueryAction() {
            @Override
            protected @NotNull AnActionEvent updateEvent(@NotNull AnActionEvent original) {
                PsiElement marked = pointer.getElement();
                if (marked == null) {
                    return original;
                }

                QueryConfiguration configuration = service.getQueryConfiguration(marked.getContainingFile());
                configuration.setQuery(query.getText());
                DataContext customContext = SimpleDataContext.builder()
                        .setParent(original.getDataContext())
                        .add(CommonDataKeys.PSI_FILE, InjectedLanguageManager.getInstance(marked.getProject()).getTopLevelFile(marked))
                        .add(CommonDataKeys.PSI_ELEMENT, marked)
                        .add(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION, configuration)
                        .build();
                return original.withDataContext(customContext);
            }
        };
    }
}
