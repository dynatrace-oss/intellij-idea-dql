package pl.thedeem.intellij.common.code;

import com.intellij.openapi.util.TextRange;

public interface InjectedLanguageHolder {
    TextRange getHostTextRange();

    default boolean isEscapingDisabled() {
        return false;
    }
}
