package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.psi.elements.StringElement;

public abstract class MultilineStringElementImpl extends DoubleQuotedStringElementImpl implements StringElement {
    public MultilineStringElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public TextRange getHostTextRange() {
        int length = getTextLength();
        return new TextRange(3, length > 6 ? getTextLength() - 3 : 3);
    }
}
