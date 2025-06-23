package pl.thedeem.intellij.dql.psi.elements;

import com.intellij.openapi.util.TextRange;

public interface StringElement extends BaseNamedElement {
    String getContent();
    TextRange getHostTextRange();
}
