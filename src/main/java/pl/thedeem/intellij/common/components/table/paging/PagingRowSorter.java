package pl.thedeem.intellij.common.components.table.paging;

import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.components.table.CommonTable;

import javax.swing.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.List;

public class PagingRowSorter extends RowSorter<TableModel> {
    private final TableRowSorter<TableModel> delegate;
    private int pageSize;
    private int currentPage;
    private int totalRowCount;

    public PagingRowSorter(@NotNull TableModel model, int pageSize) {
        this.delegate = new TableRowSorter<>(model);
        this.pageSize = pageSize;
        this.currentPage = 0;
        this.totalRowCount = model.getRowCount();

        delegate.addRowSorterListener(e -> {
            if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
                fireSortOrderChanged();
            } else {
                totalRowCount = delegate.getViewRowCount();
                currentPage = 0;
                fireRowSorterChanged(null);
            }
        });
    }

    public static PagingRowSorter install(@NotNull CommonTable table, int pageSize) {
        PagingRowSorter sorter = new PagingRowSorter(table.getModel(), pageSize);
        table.setRowSorter(sorter);
        return sorter;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        currentPage = 0;
        fireRowSorterChanged(null);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setPage(int page) {
        int clamped = Math.max(0, Math.min(page, getPageCount() - 1));
        if (clamped != currentPage) {
            currentPage = clamped;
            fireRowSorterChanged(null);
        }
    }

    public int getPageCount() {
        if (totalRowCount == 0 || pageSize <= 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalRowCount / pageSize);
    }

    @Override
    public TableModel getModel() {
        return delegate.getModel();
    }

    @Override
    public void toggleSortOrder(int column) {
        delegate.toggleSortOrder(column);
    }

    @Override
    public int convertRowIndexToModel(int index) {
        int delegateIndex = pageStart() + index;
        if (delegateIndex >= delegate.getViewRowCount()) {
            return -1;
        }
        return delegate.convertRowIndexToModel(delegateIndex);
    }

    @Override
    public int convertRowIndexToView(int index) {
        int viewIndex = delegate.convertRowIndexToView(index);
        if (viewIndex < 0) {
            return -1;
        }
        int page = viewIndex / pageSize;
        if (page != currentPage) {
            return -1;
        }
        return viewIndex - pageStart();
    }

    @Override
    public void setSortKeys(@NotNull List<? extends SortKey> keys) {
        delegate.setSortKeys(keys);
    }

    @Override
    public List<? extends SortKey> getSortKeys() {
        return delegate.getSortKeys();
    }

    @Override
    public int getViewRowCount() {
        if (pageSize <= 0) {
            return totalRowCount;
        }
        return Math.max(0, Math.min(pageSize, totalRowCount - pageStart()));
    }

    @Override
    public int getModelRowCount() {
        return delegate.getModelRowCount();
    }

    @Override
    public void modelStructureChanged() {
        delegate.modelStructureChanged();
        totalRowCount = delegate.getViewRowCount();
        currentPage = 0;
    }

    @Override
    public void allRowsChanged() {
        delegate.allRowsChanged();
        totalRowCount = delegate.getViewRowCount();
        currentPage = 0;
    }

    @Override
    public void rowsInserted(int firstRow, int endRow) {
        delegate.rowsInserted(firstRow, endRow);
        totalRowCount = delegate.getViewRowCount();
    }

    @Override
    public void rowsDeleted(int firstRow, int endRow) {
        delegate.rowsDeleted(firstRow, endRow);
        totalRowCount = delegate.getViewRowCount();
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow) {
        delegate.rowsUpdated(firstRow, endRow);
        totalRowCount = delegate.getViewRowCount();
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow, int column) {
        delegate.rowsUpdated(firstRow, endRow, column);
        totalRowCount = delegate.getViewRowCount();
    }

    private int pageStart() {
        return currentPage * pageSize;
    }
}
