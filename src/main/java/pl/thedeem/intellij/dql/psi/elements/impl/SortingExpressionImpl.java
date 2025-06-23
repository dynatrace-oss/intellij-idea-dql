package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.SortingExpression;

import java.util.Set;

public abstract class SortingExpressionImpl extends ASTWrapperPsiElement implements SortingExpression {
    public SortingExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    public Set<DQLDataType> getDataType() {
        return Set.of(DQLDataType.SORTING_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator().addPart(getText()).getFieldName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(DQLBundle.message("presentation.sortingExpression", getChildren().length), this, DQLIcon.DQL_EXPRESSION);
    }
}
