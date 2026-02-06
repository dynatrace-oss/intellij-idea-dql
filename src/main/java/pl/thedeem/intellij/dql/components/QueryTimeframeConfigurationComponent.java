package pl.thedeem.intellij.dql.components;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.ResizableTextField;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class QueryTimeframeConfigurationComponent extends JPanel {
    protected final JBTextField queryStartField;
    protected final JBTextField queryEndField;

    public QueryTimeframeConfigurationComponent() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 2));
        setOpaque(false);
        setBorder(JBUI.Borders.empty());

        queryStartField = ResizableTextField.createTimeField(
                DQLBundle.message("action.DQL.QueryConfigurationAction.timeframeFrom.placeholder"),
                DQLBundle.message("action.DQLExecutionManagerToolbar.option.queryTimeframe")
        );
        queryEndField = ResizableTextField.createTimeField(
                DQLBundle.message("action.DQL.QueryConfigurationAction.timeframeTo.placeholder"),
                DQLBundle.message("action.DQLExecutionManagerToolbar.option.queryTimeframe")
        );

        add(createPredefinedQueryTimeframeOptions());
        add(queryStartField);
        add(queryEndField);
    }

    public @NotNull JBTextField queryStartField() {
        return queryStartField;
    }

    public @NotNull JBTextField queryEndField() {
        return queryEndField;
    }

    private @NotNull JComponent createPredefinedQueryTimeframeOptions() {
        List<PredefinedTimeframe> options = List.of(
                new PredefinedTimeframe(
                        DQLBundle.message("action.DQLExecutionManagerToolbar.option.queryTimeframe.predefined.last30min"),
                        "-30m",
                        "0m"
                ),
                new PredefinedTimeframe(
                        DQLBundle.message("action.DQLExecutionManagerToolbar.option.queryTimeframe.predefined.lastHour"),
                        "-1h",
                        "0m"
                ),
                new PredefinedTimeframe(
                        DQLBundle.message("action.DQLExecutionManagerToolbar.option.queryTimeframe.predefined.last24Hours"),
                        "-24h",
                        "0m"
                ),
                new PredefinedTimeframe(
                        DQLBundle.message("action.DQLExecutionManagerToolbar.option.queryTimeframe.predefined.last7Days"),
                        "-7d",
                        "0m"
                ),
                new PredefinedTimeframe(
                        DQLBundle.message("action.DQLExecutionManagerToolbar.option.queryTimeframe.predefined.last30Days"),
                        "-30d",
                        "0m"
                ),
                new PredefinedTimeframe(
                        DQLBundle.message("action.DQLExecutionManagerToolbar.option.queryTimeframe.predefined.lastYear"),
                        "-1y",
                        "0m"
                )
        );
        DefaultActionGroup timeframePopup = new DefaultActionGroup(DQLBundle.message("action.DQLExecutionManagerToolbar.option.queryTimeframe"), true);
        timeframePopup.getTemplatePresentation().setIcon(AllIcons.General.History);
        timeframePopup.getTemplatePresentation().setPopupGroup(true);
        for (PredefinedTimeframe option : options) {
            timeframePopup.add(new AnAction(option.label()) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    queryStartField.setText(option.start());
                    queryEndField.setText(option.end());
                }
            });
        }
        DefaultActionGroup rootGroup = new DefaultActionGroup();
        rootGroup.addAction(timeframePopup);
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DQL.QueryTimeframeConfigurationPredefined", rootGroup, true);
        toolbar.setTargetComponent(this);
        JComponent toolbarComponent = toolbar.getComponent();
        toolbarComponent.setBorder(JBUI.Borders.empty());
        return toolbarComponent;
    }

    private record PredefinedTimeframe(@NotNull String label, @Nullable String start, @Nullable String end) {
    }
}
