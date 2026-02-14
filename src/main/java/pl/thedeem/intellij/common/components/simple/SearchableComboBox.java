package pl.thedeem.intellij.common.components.simple;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.ComboboxSpeedSearch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class SearchableComboBox<O> extends ComboBox<O> {
    @SuppressWarnings("unchecked")
    public SearchableComboBox(@NotNull Collection<O> options, @Nullable O initialSelection, @Nullable O emptyOption, @NotNull Consumer<O> onChange) {
        super(new CollectionComboBoxModel<>(createModelCollection(options, emptyOption)));
        ComboboxSpeedSearch.installOn(this);
        if (initialSelection != null) {
            setSelectedItem(initialSelection);
        }
        this.addActionListener(l -> {
            Object selected = getSelectedItem();
            if (selected == emptyOption) {
                onChange.accept(null);
            } else {
                onChange.accept((O) selected);
            }
        });
    }

    private static <O> List<O> createModelCollection(@NotNull Collection<O> options, @Nullable O emptyOption) {
        List<O> result = new ArrayList<>(options.size() + (emptyOption != null ? 1 : 0));
        if (emptyOption != null) {
            result.add(emptyOption);
        }
        result.addAll(options);
        return result;
    }
}
