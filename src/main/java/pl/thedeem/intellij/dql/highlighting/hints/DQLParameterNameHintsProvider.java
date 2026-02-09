package pl.thedeem.intellij.dql.highlighting.hints;

import com.intellij.codeInsight.hints.*;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLFunctionExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Provides inlay for parameters in DQL.
 * Yields a lot of experimental usages warnings, but it is the recommended way to implement this feature.
 */
public class DQLParameterNameHintsProvider implements InlayHintsProvider<DQLParameterNameHintsProvider.DQLParameterNameHintsSettings> {
    private static final SettingsKey<DQLParameterNameHintsSettings> KEY = new SettingsKey<>("dql.parameter.name.hints");

    @Override
    public @NotNull SettingsKey<DQLParameterNameHintsSettings> getKey() {
        return KEY;
    }

    @Override
    public @NotNull String getName() {
        return DQLBundle.message("settings.inlayHints.parameters.name");
    }

    @Override
    public @NotNull @Nls String getDescription() {
        return DQLBundle.message("settings.inlayHints.parameters.description");
    }

    @Override
    public @NotNull DQLParameterNameHintsSettings createSettings() {
        return new DQLParameterNameHintsSettings();
    }

    @Override
    public @NotNull ImmediateConfigurable createConfigurable(@NotNull DQLParameterNameHintsSettings settings) {
        return listener -> {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(createOptionComponent(
                    DQLBundle.message("settings.inlayHints.parameters.functions.description"),
                    settings.showFunctionHints,
                    (selected) -> {
                        settings.showFunctionHints = selected;
                        listener.settingsChanged();
                    }
            ));
            panel.add(createOptionComponent(
                    DQLBundle.message("settings.inlayHints.parameters.commands.description"),
                    settings.showCommandHints,
                    (selected) -> {
                        settings.showCommandHints = selected;
                        listener.settingsChanged();
                    }
            ));
            panel.add(createOptionComponent(
                    DQLBundle.message("settings.inlayHints.parameters.hideHintsForFirstCommandParameter.description"),
                    settings.hideHintsForFirstCommandParameter,
                    (selected) -> {
                        settings.hideHintsForFirstCommandParameter = selected;
                        listener.settingsChanged();
                    }
            ));
            return panel;
        };
    }

    @Override
    public @NotNull String getPreviewText() {
        return /* language=DQLPart */ """
                data record(f1 = 5, f2 = 10, f3 = 10, field = "10")
                 | filter matchesValue("", "value", "otherValue", caseSensitive: true)
                 | parse field, ""\"
                 INT:i
                 ""\"
                 | summarize by: {
                   i
                 }, {
                   f1 = max(f1), f2 = min(f2), count()
                 }
                 | sort i desc
                """;
    }

    @Override
    public InlayHintsCollector getCollectorFor(@NotNull PsiFile file,
                                               @NotNull Editor editor,
                                               @NotNull DQLParameterNameHintsSettings settings,
                                               @NotNull InlayHintsSink sink) {
        PresentationFactory factory = new PresentationFactory(editor);

        return new FactoryInlayHintsCollector(editor) {
            @Override
            public boolean collect(@NotNull PsiElement element, @NotNull Editor editor, @NotNull InlayHintsSink sink) {
                if (element instanceof DQLCommand command && settings.showCommandHints) {
                    addHints(factory, sink, getHints(element, command.getParameters(), settings));
                } else if (element instanceof DQLFunctionExpression function && settings.showFunctionHints) {
                    addHints(factory, sink, getHints(element, function.getParameters(), settings));
                }
                return true;
            }
        };
    }

    protected void addHints(@NotNull PresentationFactory factory,
                            @NotNull InlayHintsSink sink,
                            @NotNull List<Hint> hints) {
        for (Hint hint : hints) {
            var p = factory.inset(factory.smallTextWithoutBackground(hint.text + ": "), 0, 0, 5, 0);
            sink.addInlineElement(hint.offset, false, p, false);
        }
    }

    private static @NotNull List<Hint> getHints(@NotNull PsiElement element, @NotNull List<MappedParameter> parameters, @NotNull DQLParameterNameHintsSettings settings) {
        List<Hint> result = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            if (i == 0 && element instanceof DQLCommand && settings.hideHintsForFirstCommandParameter) {
                continue;
            }
            MappedParameter parameter = parameters.get(i);
            Parameter definition = parameter.definition();
            if (definition == null) {
                continue;
            }
            List<List<PsiElement>> groups = parameter.getParameterGroups();
            for (List<PsiElement> group : groups) {
                if (!(group.getFirst() instanceof DQLParameterExpression)) {
                    int textOffset = group.getFirst().getTextOffset();
                    if (definition.variadic()) {
                        result.add(new Hint((!parameter.included().isEmpty() ? "…" : "") + definition.name(), textOffset));
                    } else if (parameters.size() > 1) {
                        result.add(new Hint(definition.name(), textOffset));
                    }
                }
            }
        }
        return result;
    }

    protected @NotNull JComponent createOptionComponent(@NotNull String name, boolean isSelected, @NotNull Consumer<Boolean> onChange) {
        JCheckBox check = new JBCheckBox(name, isSelected);
        check.addActionListener(e -> {
            onChange.accept(check.isSelected());
        });
        return check;
    }

    protected record Hint(@NotNull String text, int offset) {
    }

    public static final class DQLParameterNameHintsSettings {
        @Attribute("showFunctionHints")
        public boolean showFunctionHints = true;

        @Attribute("showCommandHints")
        public boolean showCommandHints = true;

        @Attribute("hideHintsForFirstCommandParameter")
        public boolean hideHintsForFirstCommandParameter = true;
    }
}
