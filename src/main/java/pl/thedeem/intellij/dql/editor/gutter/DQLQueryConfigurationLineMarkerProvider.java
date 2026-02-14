package pl.thedeem.intellij.dql.editor.gutter;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.SimpleDataProviderPanel;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFile;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.editor.actions.QueryConfigurationAction;
import pl.thedeem.intellij.dql.psi.DQLQuery;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import javax.swing.*;
import java.awt.*;

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
            QueryConfigurationAction executionManager = new QueryConfigurationAction();

            BorderLayoutPanel panel = new SimpleDataProviderPanel() {
                @Override
                public void doLayout() {
                    // Resize the popup automatically - IntelliJ does not do it
                    super.doLayout();
                    JBPopup popup = PopupUtil.getPopupContainerFor(this);

                    if (popup != null && !popup.isDisposed() && popup.isVisible()) {
                        Dimension preferred = this.getPreferredSize();
                        Dimension actual = popup.getSize();
                        if (preferred.width != actual.width) {
                            SwingUtilities.invokeLater(() -> {
                                if (!popup.isDisposed()) {
                                    popup.pack(true, true);
                                }
                            });
                        }
                    }
                }

                @Override
                public void uiDataSnapshot(@NotNull DataSink dataSink) {
                    DQLQueryConfigurationService service = DQLQueryConfigurationService.getInstance();
                    dataSink.lazy(CommonDataKeys.PSI_FILE, element::getContainingFile);
                    dataSink.lazy(CommonDataKeys.PSI_ELEMENT, () -> element);
                    dataSink.set(QueryConfigurationAction.SHOW_QUERY_EXECUTE_BUTTON, false);
                    dataSink.set(QueryConfigurationAction.SHOW_QUERY_VALIDATION_OPTION, false);
                    dataSink.lazy(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION, () -> service.getQueryConfiguration(element.getContainingFile()));
                }
            };
            panel.setBorder(JBUI.Borders.empty(5));
            JComponent component = executionManager.createCustomComponent(new Presentation(), "DQL.GutterPopup");
            panel.addToCenter(component);

            JBPopup popup = JBPopupFactory.getInstance()
                    .createComponentPopupBuilder(panel, panel)
                    .setRequestFocus(true)
                    .setResizable(false)
                    .setMovable(true)
                    .setCancelOnClickOutside(true)
                    .setDimensionServiceKey(null, "DQL.GutterPopup.NoCache", false)
                    .createPopup();
            popup.show(new RelativePoint(mouseEvent));
        };
    }
}
