package pl.thedeem.intellij.common.components.table.reordering;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

class ReorderableListTransferHandler extends TransferHandler {
    private static final DataFlavor FLAVOR = DataFlavor.stringFlavor;
    private int fromIndex = -1;

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    protected Transferable createTransferable(@NotNull JComponent c) {
        if (c instanceof JList<?> list) {
            fromIndex = list.getSelectedIndex();
            if (list.getSelectedValue() instanceof TableColumnItem item) {
                return new StringSelection(item.name());
            }
        }
        return new StringSelection("");
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDrop() && support.isDataFlavorSupported(FLAVOR);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        if (support.getComponent() instanceof JList<?> list && support.getDropLocation() instanceof JList.DropLocation dropLocation) {
            if (list.getModel() instanceof DefaultListModel<?> model) {
                int toIndex = dropLocation.getIndex();
                if (fromIndex < 0 || toIndex < 0) {
                    return false;
                }
                if (toIndex > model.getSize()) {
                    toIndex = model.getSize();
                }

                if (toIndex == fromIndex || toIndex == fromIndex + 1) {
                    return false;
                }

                if (model.get(fromIndex) instanceof TableColumnItem tableItem) {
                    model.remove(fromIndex);
                    if (toIndex > fromIndex) {
                        toIndex--;
                    }
                    ((DefaultListModel<Object>) model).add(toIndex, tableItem);
                    list.setSelectedIndex(toIndex);
                    return true;
                }
            }
        }
        return false;
    }
}