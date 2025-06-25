package pl.thedeem.intellij.dql.components;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.JBPopupMenu;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultEditorKit;

public class DQLComponentUtils {
  public static final Border DEFAULT_BORDER = BorderFactory.createEmptyBorder(5, 5, 5, 5);

  public static JBPopupMenu createDefaultPopupMenu() {
    JBPopupMenu popupMenu = new JBPopupMenu();

    JMenuItem pasteAction = new JMenuItem(new DefaultEditorKit.PasteAction());
    pasteAction.setIcon(AllIcons.Actions.MenuPaste);
    pasteAction.setText(DQLBundle.message("components.popupMenu.actions.paste"));
    popupMenu.add(pasteAction);

    return popupMenu;
  }
}
