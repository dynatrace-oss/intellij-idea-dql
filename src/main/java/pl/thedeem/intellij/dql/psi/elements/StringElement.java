package pl.thedeem.intellij.dql.psi.elements;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiLanguageInjectionHost;

public interface StringElement extends BaseNamedElement, PsiLanguageInjectionHost {
    String getContent();

    TextRange getHostTextRange();
}
