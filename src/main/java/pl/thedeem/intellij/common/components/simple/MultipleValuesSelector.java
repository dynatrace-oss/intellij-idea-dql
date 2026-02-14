package pl.thedeem.intellij.common.components.simple;

import com.intellij.ui.CheckBoxList;
import com.intellij.ui.ListSpeedSearch;
import com.intellij.util.PairConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MultipleValuesSelector<O> extends CheckBoxList<O> {
    public MultipleValuesSelector(
            int selectionMode,
            @NotNull Collection<O> options,
            @NotNull Collection<O> defaultOptions,
            @NotNull PairConsumer<O, Boolean> onChange
    ) {
        setSelectionMode(selectionMode);
        for (O option : options) {
            addItem(option, option.toString(), defaultOptions.contains(option));
        }
        setCheckBoxListListener((index, value) -> onChange.consume(getItemAt(index), value));
        ListSpeedSearch.installOn(this, Object::toString);
    }
}
