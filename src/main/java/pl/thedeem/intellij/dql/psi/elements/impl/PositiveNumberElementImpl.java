package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.NumberElement;
import org.jetbrains.annotations.NotNull;

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
        return new DQLFieldNamesGenerator().addPart(getName()).getFieldName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(this.getName(), this, DQLIcon.DQL_NUMBER);
    }

    @Override
    public Number getValue() {
        String text = getName();
        try {
            if (text != null) {
                if (text.contains(".")) {
                    return Double.valueOf(text);
                }
                return Long.valueOf(text);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    @Override
    public Set<DQLDataType> getDataType() {
        Number value = getValue();
        return switch (value) {
            case Double ignored -> Set.of(DQLDataType.POSITIVE_DOUBLE);
            case Long ignored -> Set.of(DQLDataType.POSITIVE_LONG);
            default -> Set.of(DQLDataType.ANY);
        };
    }

    @Override
    public boolean accessesData() {
        return false;
    }
}
