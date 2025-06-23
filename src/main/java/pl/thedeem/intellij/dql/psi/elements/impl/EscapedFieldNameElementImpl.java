package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import pl.thedeem.intellij.dql.psi.DQLStringContentElement;
import pl.thedeem.intellij.dql.psi.elements.FieldElement;
import org.jetbrains.annotations.NotNull;

public abstract class EscapedFieldNameElementImpl extends FieldNameElementImpl implements FieldElement {
    public EscapedFieldNameElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        DQLStringContentElement content = this.findChildByClass(DQLStringContentElement.class);
        return content != null ? content.getText() : this.getText();
    }
}
