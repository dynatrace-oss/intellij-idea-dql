package pl.thedeem.intellij.common.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class CustomPopupAction extends DefaultActionGroup {
    private final Supplier<JBPopup> popupSupplier;

    public CustomPopupAction(
            @NotNull Supplier<@NlsActions.ActionText String> name,
            @Nullable Icon icon,
            @NotNull Supplier<JBPopup> popupSupplier
    ) {
        super(name, true);
        this.popupSupplier = popupSupplier;
        Presentation templatePresentation = getTemplatePresentation();
        templatePresentation.setPerformGroup(true);
        if (icon != null) {
            templatePresentation.setIcon(icon);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        super.actionPerformed(e);
        Component c = e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        if (e.getInputEvent() != null && e.getInputEvent().getComponent() != null) {
            c = e.getInputEvent().getComponent();
        }
        JBPopup popup = popupSupplier.get();
        if (c != null && popup != null) {
            popup.showUnderneathOf(c);
        }
    }
}
