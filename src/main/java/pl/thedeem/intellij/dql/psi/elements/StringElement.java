package pl.thedeem.intellij.dql.psi.elements;

import com.intellij.psi.PsiLanguageInjectionHost;
import pl.thedeem.intellij.common.code.InjectedLanguageHolder;

public interface StringElement extends BaseNamedElement, PsiLanguageInjectionHost, InjectedLanguageHolder {
    String getContent();
}
