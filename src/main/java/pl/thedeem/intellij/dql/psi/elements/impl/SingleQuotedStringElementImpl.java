package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import pl.thedeem.intellij.dql.psi.elements.StringElement;
import org.jetbrains.annotations.NotNull;

public abstract class SingleQuotedStringElementImpl extends DoubleQuotedStringElementImpl implements StringElement {
    public SingleQuotedStringElementImpl(@NotNull ASTNode node) {
        super(node);
    }
}
