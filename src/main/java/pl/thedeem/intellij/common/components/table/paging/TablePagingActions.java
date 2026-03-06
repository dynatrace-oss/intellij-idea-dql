package pl.thedeem.intellij.common.components.table.paging;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;

public class TablePagingActions extends DefaultActionGroup {
    private static final int[] PAGE_SIZES = {100, 250, 500, 1000, 5000};

    public TablePagingActions(@NotNull PagingRowSorter sorter) {
        add(new AnAction(DQLBundle.message("components.results.table.paging.prevPage.tooltip"), null, AllIcons.Actions.Back) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                sorter.setPage(sorter.getCurrentPage() - 1);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setVisible(sorter.getPageCount() > 1);
                e.getPresentation().setEnabled(sorter.getCurrentPage() > 0);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        });

        add(new ComboBoxAction() {
            @Override
            public void update(@NotNull AnActionEvent e) {
                if (e.isFromContextMenu()) {
                    e.getPresentation().setEnabledAndVisible(false);
                    return;
                }
                e.getPresentation().setVisible(sorter.getPageCount() > 1);
                e.getPresentation().setText(DQLBundle.message("components.results.table.paging.page.item", sorter.getCurrentPage() + 1, sorter.getPageCount()));
                e.getPresentation().setDescription(DQLBundle.message("components.results.table.paging.page.tooltip"));
            }

            @Override
            protected @NotNull DefaultActionGroup createPopupActionGroup(@NotNull JComponent button, @NotNull DataContext dataContext) {
                DefaultActionGroup group = new DefaultActionGroup();
                int pageCount = sorter.getPageCount();
                for (int i = 0; i < pageCount; i++) {
                    int page = i;
                    group.add(new AnAction(DQLBundle.message("components.results.table.paging.page.item", i + 1, sorter.getPageCount())) {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e) {
                            sorter.setPage(page);
                        }

                        @Override
                        public @NotNull ActionUpdateThread getActionUpdateThread() {
                            return ActionUpdateThread.BGT;
                        }
                    });
                }
                return group;
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        });

        add(new AnAction(DQLBundle.message("components.results.table.paging.nextPage.tooltip"), null, AllIcons.Actions.Forward) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                sorter.setPage(sorter.getCurrentPage() + 1);
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setVisible(sorter.getPageCount() > 1);
                e.getPresentation().setEnabled(sorter.getCurrentPage() < sorter.getPageCount() - 1);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        });

        add(new ComboBoxAction() {
            @Override
            public void update(@NotNull AnActionEvent e) {
                if (e.isFromContextMenu()) {
                    e.getPresentation().setEnabledAndVisible(false);
                    return;
                }
                e.getPresentation().setText(DQLBundle.message(
                        "components.results.table.paging.pageSizeAction.text",
                        sorter.getPageSize()
                ));
                e.getPresentation().setDescription(DQLBundle.message("components.results.table.paging.pageSizeAction.description"));
            }

            @Override
            protected @NotNull DefaultActionGroup createPopupActionGroup(@NotNull JComponent button, @NotNull DataContext dataContext) {
                DefaultActionGroup group = new DefaultActionGroup();
                for (int size : PAGE_SIZES) {
                    group.add(new AnAction(DQLBundle.message("components.results.table.paging.pageSizeAction.option", size)) {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e) {
                            sorter.setPageSize(size);
                        }

                        @Override
                        public @NotNull ActionUpdateThread getActionUpdateThread() {
                            return ActionUpdateThread.BGT;
                        }
                    });
                }
                return group;
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        });
    }
}
