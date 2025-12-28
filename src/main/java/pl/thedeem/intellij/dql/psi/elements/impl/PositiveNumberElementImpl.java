package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.elements.NumberElement;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

import java.util.Set;

public abstract class PositiveNumberElementImpl extends ASTWrapperPsiElement implements NumberElement {
    public PositiveNumberElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance(getProject()).calculateFieldName(getText());
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(this.getName(), this, DQLIcon.DQL_NUMBER);
    }

    @Override
    public @NotNull Set<String> getDataType() {
        String text = getText();
        if (text.contains(".")) {
            return Set.of("dql.dataType.double");
        } else {
            return Set.of("dql.dataType.long");
        }
    }

    @Override
    public boolean accessesData() {
        return false;
    }
}
