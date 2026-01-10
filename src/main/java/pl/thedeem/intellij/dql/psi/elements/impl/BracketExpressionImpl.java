package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.BracketExpression;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BracketExpressionImpl extends ASTWrapperPsiElement implements BracketExpression {

    public BracketExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Collection<String> getDataType() {
        PsiElement[] children = this.getChildren();

        Set<String> dataTypes = new HashSet<>();
        if (children.length > 0) {
            for (PsiElement child : getChildren()) {
                if (child instanceof BaseElement el) {
                    dataTypes.addAll(el.getDataType());
                }
            }
        }
        // If the array is empty, it should match all expression types
        return dataTypes;
    }

    @Override
    public boolean accessesData() {
        for (PsiElement param : getChildren()) {
            if (param instanceof BaseTypedElement entity && entity.accessesData()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.bracketExpression", getChildren().length), this, DQLIcon.DQL_BOOLEAN);
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance().calculateFieldName(
                "{",
                new DQLFieldNamesService.SeparatedChildren(List.of(getChildren()), ","),
                "}"
        );
    }
}
