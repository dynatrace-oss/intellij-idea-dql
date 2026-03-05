package pl.thedeem.intellij.common.components;

import com.intellij.openapi.actionSystem.AnAction;
import org.jetbrains.annotations.NotNull;

public interface PanelWithToolbarActions {
    @NotNull AnAction[] getToolbarActions();
}
