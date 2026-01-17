package pl.thedeem.intellij.dql.editor;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import java.util.List;

public class DQLInjectionLineMakerProvider extends RunLineMarkerContributor implements DumbAware {
    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        if (!DQLSettings.getInstance().isDQLInjectionGutterIconVisible() || element.getFirstChild() != null) {
            return null;
        }
        InjectedLanguageManager injector = InjectedLanguageManager.getInstance(element.getProject());
        PsiLanguageInjectionHost host = PsiTreeUtil.getParentOfType(element, PsiLanguageInjectionHost.class);
        if (host == null || element != firstLeafOf(host)) {
            return null;
        }

        List<Pair<PsiElement, TextRange>> files = injector.getInjectedPsiFiles(host);
        if (files != null) {
            for (Pair<PsiElement, TextRange> pair : files) {
                PsiFile file = pair.first.getContainingFile();
                if (DQLFileType.INSTANCE.equals(file.getFileType())) {
                    DQLQueryConfigurationService service = DQLQueryConfigurationService.getInstance();
                    QueryConfiguration configuration = service.getQueryConfiguration(file);
                    AnAction wrappedAction = createCustomAction(file, configuration);
                    return new Info(wrappedAction);
                }
            }
        }
        return null;
    }

    private static @NotNull AnAction createCustomAction(PsiFile file, QueryConfiguration configuration) {
        AnAction originalAction = ActionManager.getInstance().getAction("DQL.StartStopExecution");
        AnAction wrappedAction = new AnAction() {
            @Override
            public void update(@NotNull AnActionEvent e) {
                originalAction.update(createCustomEvent(e));
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                originalAction.actionPerformed(createCustomEvent(e));
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            private @NotNull AnActionEvent createCustomEvent(@NotNull AnActionEvent original) {
                DataContext customContext = SimpleDataContext.builder()
                        .setParent(original.getDataContext())
                        .add(CommonDataKeys.PSI_FILE, file)
                        .add(CommonDataKeys.PSI_ELEMENT, file)
                        .add(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION, configuration)
                        .build();

                return original.withDataContext(customContext);
            }
        };
        wrappedAction.copyFrom(originalAction);
        return wrappedAction;
    }


    private static @NotNull PsiElement firstLeafOf(@NotNull PsiLanguageInjectionHost host) {
        PsiElement leaf = host;
        while (leaf.getFirstChild() != null) {
            leaf = leaf.getFirstChild();
        }
        return leaf;
    }

}
