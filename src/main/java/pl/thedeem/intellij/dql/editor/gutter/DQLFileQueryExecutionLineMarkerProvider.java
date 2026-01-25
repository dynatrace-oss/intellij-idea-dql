package pl.thedeem.intellij.dql.editor.gutter;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.actionSystem.toolbarLayout.ToolbarLayoutStrategy;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFile;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.editor.actions.ExecuteDQLQueryAction;
import pl.thedeem.intellij.dql.editor.actions.ExecutionManagerAction;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLCommandKeyword;
import pl.thedeem.intellij.dql.psi.DQLQuery;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.Consumer;

public class DQLFileQueryExecutionLineMarkerProvider extends LineMarkerProviderDescriptor implements LineMarkerProvider, DumbAware {
    @Override
    public String getName() {
        return DQLBundle.message("gutter.executeDQL.wholeQuery.name");
    }

    @Override
    public @NotNull Icon getIcon() {
        return AllIcons.Actions.Execute;
    }

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
        if (query == null) {
            return null;
        }

        if (!isEnabled(query)) {
            return null;
        }

        DQLQueryConfigurationService service = DQLQueryConfigurationService.getInstance();
        SmartPsiElementPointer<PsiElement> pointer = SmartPointerManager.createPointer(element);
        ExecuteDQLQueryAction execute = new ExecuteDQLQueryAction() {
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

        return new LineMarkerInfo<>(
                element,
                element.getTextRange(),
                getIcon(),
                it -> getName(),
                createNavigationHandler(execute, element),
                GutterIconRenderer.Alignment.RIGHT,
                this::getName
        ) {
            @Override
            public GutterIconRenderer createGutterRenderer() {
                if (!isDqlFile(element)) {
                    return super.createGutterRenderer();
                }
                return new LineMarkerInfo.LineMarkerGutterIconRenderer<>(this) {
                    @Override
                    public AnAction getClickAction() {
                        return execute;
                    }

                    @Override
                    public boolean isNavigateAction() {
                        return true;
                    }
                };
            }
        };
    }

    protected boolean isEnabled(@NotNull DQLQuery query) {
        return query.getParent() instanceof DQLFile && isDqlFile(query);
    }

    protected boolean isDqlFile(@NotNull PsiElement element) {
        PsiFile topLevelFile = InjectedLanguageManager.getInstance(element.getProject()).getTopLevelFile(element);
        return DQLFileType.INSTANCE.equals(topLevelFile.getVirtualFile().getFileType());
    }

    private <T extends PsiElement> @Nullable GutterIconNavigationHandler<T> createNavigationHandler(@NotNull AnAction mainAction, @NotNull T element) {
        // For DQL files, let the original Gutter renderer do its work
        if (isDqlFile(element)) {
            return null;
        }
        // For injected DQL fragments, we need to show more options for the user
        return (mouseEvent, elt) -> {
            DefaultActionGroup group = new DefaultActionGroup();
            ExecutionManagerAction executionManager = new ExecutionManagerAction(element.getContainingFile());
            group.add(mainAction);
            group.addSeparator();
            group.add(executionManager);

            JPanel panel = new ToolbarPanel() {
                @Override
                public void uiDataSnapshot(@NotNull DataSink dataSink) {
                    DQLQueryConfigurationService service = DQLQueryConfigurationService.getInstance();
                    dataSink.set(CommonDataKeys.PSI_FILE, InjectedLanguageManager.getInstance(element.getProject()).getTopLevelFile(element));
                    dataSink.set(CommonDataKeys.PSI_ELEMENT, element);
                    QueryConfiguration configuration = service.getQueryConfiguration(element.getContainingFile());
                    dataSink.set(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION, configuration);
                }
            };
            panel.setBorder(JBUI.Borders.empty(5));

            ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DQL.GutterPopup", group, true);
            toolbar.setLayoutStrategy(ToolbarLayoutStrategy.NOWRAP_STRATEGY);
            toolbar.setTargetComponent(panel);

            JComponent toolbarComponent = toolbar.getComponent();
            toolbarComponent.setBorder(JBUI.Borders.empty());
            toolbarComponent.setOpaque(false);
            panel.add(toolbarComponent, BorderLayout.CENTER);

            JBPopup popup = JBPopupFactory.getInstance()
                    .createComponentPopupBuilder(panel, toolbarComponent)
                    .setRequestFocus(true)
                    .setResizable(false)
                    .setMovable(true)
                    .setCancelOnClickOutside(true)
                    .createPopup();

            popup.show(new RelativePoint(mouseEvent));
            toolbarComponent.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        if (!popup.isDisposed()) {
                            popup.pack(true, true);
                        }
                    });
                }
            });
            Consumer<AnActionEvent> listener = (e) -> SwingUtilities.invokeLater(() -> {
                if (!popup.isDisposed()) {
                    popup.pack(true, true);
                }
            });
            executionManager.addActionListener(listener);

            Disposer.register(popup, () -> executionManager.removeActionListener(listener));
        };
    }

    private abstract static class ToolbarPanel extends BorderLayoutPanel implements UiDataProvider {
    }
}
