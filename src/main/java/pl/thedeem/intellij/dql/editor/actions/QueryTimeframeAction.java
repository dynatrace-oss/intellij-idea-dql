package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.util.List;

public abstract class QueryTimeframeAction extends AnAction implements DumbAware, CustomComponentAction {
    private final String defaultStart;
    private final String defaultEnd;

    public QueryTimeframeAction(@Nullable String defaultStart, @Nullable String defaultEnd) {
        this.defaultStart = defaultStart;
        this.defaultEnd = defaultEnd;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    }

    public abstract void timeframeStartChanged(@NotNull AnActionEvent e, @Nullable String value);

    public abstract void timeframeEndChanged(@NotNull AnActionEvent e, @Nullable String value);

    public abstract void timeframeChanged(@NotNull AnActionEvent e, @Nullable String start, @Nullable String end);

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        BorderLayoutPanel panel = JBUI.Panels.simplePanel();
        panel.setOpaque(false);
        panel.setBorder(JBUI.Borders.empty());
        DefaultActionGroup actions = new DefaultActionGroup();

        AbstractTimeFieldAction queryStartAction = new AbstractTimeFieldAction(
                defaultStart,
                DQLBundle.message("action.DQL.QueryConfigurationAction.timeframeFrom.placeholder"),
                DQLBundle.message("action.DQLExecutionManagerToolbar.option.queryTimeframe"),
                null
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                timeframeStartChanged(e, getValue());
            }
        };
        AbstractTimeFieldAction queryEndAction = new AbstractTimeFieldAction(
                defaultEnd,
                DQLBundle.message("action.DQL.QueryConfigurationAction.timeframeTo.placeholder"),
                DQLBundle.message("action.DQLExecutionManagerToolbar.option.queryTimeframe"),
                null
        ) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                timeframeEndChanged(e, getValue());
            }
        };
        DefaultActionGroup timeframePopup = createPredefinedQueryTimeframeOptions(queryStartAction, queryEndAction);

        actions.add(timeframePopup);
        actions.add(queryStartAction);
        actions.add(queryEndAction);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DQL.QueryTimeframeAction", actions, true);
        toolbar.setTargetComponent(panel);
        panel.addToCenter(toolbar.getComponent());
        toolbar.getComponent().setOpaque(false);
        toolbar.getComponent().setBorder(JBUI.Borders.empty());
        return panel;
    }

    private @NotNull DefaultActionGroup createPredefinedQueryTimeframeOptions(
            @NotNull AbstractTimeFieldAction queryStartAction,
            @NotNull AbstractTimeFieldAction queryEndAction
    ) {
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
        for (PredefinedTimeframe option : options) {
            timeframePopup.add(new AnAction(option.label()) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    queryStartAction.updateValue(option.start());
                    queryEndAction.updateValue(option.end());
                    timeframeChanged(e, option.start(), option.end());
                }
            });
        }
        return timeframePopup;
    }

    private record PredefinedTimeframe(@NotNull String label, @Nullable String start, @Nullable String end) {
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
