package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.BracketExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public abstract class BracketExpressionImpl extends ASTWrapperPsiElement implements BracketExpression {

    public BracketExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public Set<DQLDataType> getDataType() {
        PsiElement[] children = this.getChildren();
        if (children.length > 0) {
            return DQLUtil.calculateFieldType(children);
        }
        // If the array is empty, it should match all expression types
        return Set.of(
                DQLDataType.ARRAY,
                DQLDataType.SORTING_EXPRESSION,
                DQLDataType.WRITE_ONLY_EXPRESSION,
                DQLDataType.READ_ONLY_EXPRESSION
        );
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
        return new DQLFieldNamesGenerator()
                .addPart("{")
                .addPart(List.of(getChildren()), ", ")
                .addPart("}")
                .getFieldName();
    }
}
