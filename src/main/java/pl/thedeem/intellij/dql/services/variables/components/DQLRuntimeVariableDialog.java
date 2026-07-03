package pl.thedeem.intellij.dql.services.variables.components;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DQLRuntimeVariableDialog extends DialogWrapper {
    private final List<VariableDef> myVariables;
    private final List<VariableRow> myRows = new ArrayList<>();

    private enum VariableType {
        NULL("dql.dataType.null"),
        STRING("dql.dataType.string"),
        BOOLEAN("dql.dataType.boolean"),
        NUMBER("dql.dataType.double", "dql.dataType.long"),
        ARRAY("dql.dataType.array"),
        RECORD("dql.dataType.record"),
        FRAGMENT();

        private final String[] myDataTypes;

        VariableType(String... types) {
            myDataTypes = types;
        }

        @NotNull
        public Set<String> getDataTypes() {
            return Set.of(myDataTypes);
        }

        @NotNull
        public static VariableType fromDataTypes(@NotNull Collection<String> dataTypes) {
            for (VariableType type : values()) {
                for (String t : type.myDataTypes) {
                    if (dataTypes.contains(t)) return type;
                }
            }
            return FRAGMENT;
        }
    }

    private static final class VariableDef {
        private final @NotNull String name;
        private final @NotNull VariableType type;
        private final @Nullable String value;

        private VariableDef(@NotNull DQLVariablesService.VariableDefinition definition) {
            this.name = definition.name();
            this.type = definition.value() != null ? VariableType.fromDataTypes(definition.dataTypes()) : VariableType.NULL;
            this.value = definition.value();
        }
    }

    private static final class VariableRow {
        private static final String NULL_CARD = "null";
        private static final String VALUE_CARD = "value";

        private final @NotNull String myName;
        private final @NotNull ComboBox<VariableType> myTypeCombo;
        private final @NotNull JBTextField myValueField;
        private final @NotNull JPanel myValueContainer;
        private final @NotNull CardLayout myValueLayout;

        private VariableRow(@NotNull VariableDef definition) {
            myName = definition.name;

            myTypeCombo = new ComboBox<>(VariableType.values());
            myTypeCombo.setRenderer(SimpleListCellRenderer.create("", type -> StringUtil.capitalize(type.name().toLowerCase(Locale.ROOT))));
            myTypeCombo.setSelectedItem(definition.type);

            String initialValue = definition.type == VariableType.STRING ? StringUtil.unquoteString(Objects.requireNonNullElse(definition.value, "")) : definition.value;
            myValueField = new JBTextField(StringUtil.notNullize(initialValue));

            myValueLayout = new CardLayout();
            myValueContainer = new JPanel(myValueLayout);
            myValueContainer.add(new JBLabel(DQLVariablesServiceImpl.NULL_VARIABLE_PLACEHOLDER, UIUtil.ComponentStyle.SMALL), NULL_CARD);
            myValueContainer.add(myValueField, VALUE_CARD);

            myTypeCombo.addActionListener(e -> updateValueCard());
            updateValueCard();
        }

        private void updateValueCard() {
            VariableType selected = (VariableType) myTypeCombo.getSelectedItem();
            myValueLayout.show(myValueContainer, selected == VariableType.NULL ? NULL_CARD : VALUE_CARD);
        }

        @NotNull
        public DQLVariablesService.VariableDefinition toDefinition() {
            VariableType type = (VariableType) myTypeCombo.getSelectedItem();
            String value = type == VariableType.NULL ? null : myValueField.getText();
            if (type == VariableType.STRING) {
                value = value != null ? "\"" + value + "\"" : null;
            }
            return new DQLVariablesService.VariableDefinition(myName, value, type != null ? type.getDataTypes() : Set.of());
        }
    }

    public DQLRuntimeVariableDialog(@NotNull Project project, @NotNull Collection<DQLVariablesService.VariableDefinition> variables) {
        super(project, true);
        setTitle(DQLBundle.message("dialog.runtimeVariables.title"));

        Set<String> processedNames = new HashSet<>();
        myVariables = variables.stream()
                .sorted(Comparator.comparing(DQLVariablesService.VariableDefinition::name, String.CASE_INSENSITIVE_ORDER))
                .filter(v -> processedNames.add(v.name()))
                .map(VariableDef::new)
                .toList();
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        int typeColumnWidth = JBUI.scale(140);
        FormBuilder formBuilder = FormBuilder.createFormBuilder();

        formBuilder.addLabeledComponent(
                new JBLabel(DQLBundle.message("dialog.runtimeVariables.name"), UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER),
                createFieldRow(
                        new JBLabel(DQLBundle.message("dialog.runtimeVariables.type"), UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER),
                        new JBLabel(DQLBundle.message("dialog.runtimeVariables.value"), UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER),
                        typeColumnWidth
                )
        );

        for (VariableDef definition : myVariables) {
            VariableRow row = new VariableRow(definition);
            myRows.add(row);
            JBLabel l = new JBLabel("$" + DQLBundle.shorten(row.myName, 25));
            l.setToolTipText(row.myName);
            formBuilder.addLabeledComponent(l, createFieldRow(row.myTypeCombo, row.myValueContainer, typeColumnWidth));
        }

        JPanel formPanel = formBuilder.getPanel();
        JBScrollPane scrollPane = new JBScrollPane(formPanel);
        scrollPane.setBorder(JBUI.Borders.empty());
        return JBUI.Panels.simplePanel().addToTop(scrollPane);
    }

    private static @NotNull JComponent createFieldRow(@NotNull JComponent type, @NotNull JComponent value, int typeColumnWidth) {
        type.setPreferredSize(new Dimension(typeColumnWidth, type.getPreferredSize().height));
        value.setPreferredSize(new Dimension(JBUI.scale(150), value.getPreferredSize().height));
        return JBUI.Panels.simplePanel(JBUI.scale(8), 0)
                .addToLeft(type)
                .addToCenter(value);
    }

    public @NotNull List<DQLVariablesService.VariableDefinition> getResult() {
        return myRows.stream()
                .map(VariableRow::toDefinition)
                .toList();
    }
}