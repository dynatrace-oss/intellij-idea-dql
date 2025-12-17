package pl.thedeem.intellij.dpl.style;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DynatracePatternLanguage;

public class DPLCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
    @Override
    public CustomCodeStyleSettings createCustomSettings(@NotNull CodeStyleSettings settings) {
        return new DPLCodeStyleSettings(settings);
    }

    @Override
    public String getConfigurableDisplayName() {
        return DynatracePatternLanguage.DPL_DISPLAY_NAME;
    }

    @Override
    @NotNull
    public CodeStyleConfigurable createConfigurable(@NotNull CodeStyleSettings settings,
                                                    @NotNull CodeStyleSettings modelSettings) {
        return new CodeStyleAbstractConfigurable(settings, modelSettings, this.getConfigurableDisplayName()) {
            @Override
            protected @NotNull CodeStyleAbstractPanel createPanel(@NotNull CodeStyleSettings settings) {
                return new DPLCodeStyleMainPanel(getCurrentSettings(), settings);
            }
        };
    }

    @Override
    public @Nullable Language getLanguage() {
        return DynatracePatternLanguage.INSTANCE;
    }

    private static class DPLCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
        public DPLCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
            super(DynatracePatternLanguage.INSTANCE, currentSettings, settings);
        }
    }
}
