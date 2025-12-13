package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.elements.NumberElement;

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
        return new StandardItemPresentation(this.getName(), this, DQLIcon.DQL_NUMBER);
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
    public @NotNull Set<String> getDataType() {
        Number value = getValue();
        return switch (value) {
            case Double ignored -> Set.of("dql.dataType.double");
            case Long ignored -> Set.of("dql.dataType.long");
            default -> Set.of("dql.dataType.double", "dql.dataType.long");
        };
    }

    @Override
    public boolean accessesData() {
        return false;
    }
}
