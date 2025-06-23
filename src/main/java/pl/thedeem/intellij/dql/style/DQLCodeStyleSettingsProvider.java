package pl.thedeem.intellij.dql.style;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import org.jetbrains.annotations.NotNull;

public class DQLCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
    @Override
    public CustomCodeStyleSettings createCustomSettings(@NotNull CodeStyleSettings settings) {
        return new DQLCodeStyleSettings(settings);
    }

    @Override
    public String getConfigurableDisplayName() {
        return DynatraceQueryLanguage.DQL_DISPLAY_NAME;
    }

    @NotNull
    public CodeStyleConfigurable createConfigurable(@NotNull CodeStyleSettings settings,
                                                    @NotNull CodeStyleSettings modelSettings) {
        return new CodeStyleAbstractConfigurable(settings, modelSettings, this.getConfigurableDisplayName()) {
            @Override
            protected @NotNull CodeStyleAbstractPanel createPanel(@NotNull CodeStyleSettings settings) {
                return new DQLCodeStyleMainPanel(getCurrentSettings(), settings);
            }
        };
    }

    private static class DQLCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
        public DQLCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
            super(DynatraceQueryLanguage.INSTANCE, currentSettings, settings);
        }
    }
}
