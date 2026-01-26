package pl.thedeem.intellij.dql.editor.gutter;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.toolbarLayout.ToolbarLayoutStrategy;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFile;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.editor.actions.ExecutionManagerAction;
import pl.thedeem.intellij.dql.psi.DQLQuery;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.Consumer;

public class DQLQueryConfigurationLineMarkerProvider extends AbstractDQLQueryLineMarkerProvider {
    @Override
    public String getName() {
        return DQLBundle.message("gutter.executeDQL.configure.name");
    }

    @Override
    public @NotNull Icon getIcon() {
        return DQLIcon.GUTTER_EXECUTE_SETTINGS;
    }

    @Override
    protected boolean isEnabled(@NotNull PsiElement element, @NotNull DQLQuery query) {
        return query.getParent() instanceof DQLFile && !isDqlFile(query);
    }

    @Override
    protected @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element, @NotNull DQLQuery query) {
        return new LineMarkerInfo<>(
                element,
                element.getTextRange(),
                getIcon(),
                it -> getName(),
                createNavigationHandler(),
                GutterIconRenderer.Alignment.RIGHT,
                this::getName
        );
    }

    private @NotNull GutterIconNavigationHandler<PsiElement> createNavigationHandler() {
        return (mouseEvent, element) -> {
            DefaultActionGroup group = new DefaultActionGroup();
            ExecutionManagerAction executionManager = new ExecutionManagerAction(element.getContainingFile(), false);
            group.add(executionManager);

            JPanel panel = new ToolbarPanel() {
                @Override
                public void uiDataSnapshot(@NotNull DataSink dataSink) {
                    DQLQueryConfigurationService service = DQLQueryConfigurationService.getInstance();
                    dataSink.lazy(CommonDataKeys.PSI_FILE, () -> InjectedLanguageManager.getInstance(element.getProject()).getTopLevelFile(element));
                    dataSink.lazy(CommonDataKeys.PSI_ELEMENT, () -> element);
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
