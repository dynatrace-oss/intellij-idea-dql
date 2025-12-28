package pl.thedeem.intellij.dql.components.editor;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import java.util.List;

public class DQLInjectionLineMakerProvider extends RunLineMarkerContributor {
    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiLanguageInjectionHost host) || !DQLSettings.getInstance().isDQLInjectionGutterIconVisible()) {
            return null;
        }
        InjectedLanguageManager injector = InjectedLanguageManager.getInstance(host.getProject());
        List<Pair<PsiElement, TextRange>> files = injector.getInjectedPsiFiles(host);
        if (files != null) {
            for (Pair<PsiElement, TextRange> pair : files) {
                PsiFile file = pair.first.getContainingFile();
                if (DQLFileType.INSTANCE.equals(file.getFileType())) {
                    AnAction originalAction = ActionManager.getInstance().getAction("DQL.RunQuery");
                    AnAction wrappedAction = new AnAction() {
                        @Override
                        public void update(@NotNull AnActionEvent e) {
                            originalAction.update(e);
                        }

                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e) {
                            DataContext customContext = SimpleDataContext.builder()
                                    .setParent(e.getDataContext())
                                    .add(CommonDataKeys.PSI_FILE, file)
                                    .add(CommonDataKeys.PSI_ELEMENT, file)
                                    .build();

                            AnActionEvent newEvent = e.withDataContext(customContext);
                            originalAction.actionPerformed(newEvent);
                        }

                        @Override
                        public @NotNull ActionUpdateThread getActionUpdateThread() {
                            return ActionUpdateThread.BGT;
                        }
                    };
                    wrappedAction.copyFrom(originalAction);
                    return new Info(wrappedAction);
                }
            }
        }
        return null;
    }
}
