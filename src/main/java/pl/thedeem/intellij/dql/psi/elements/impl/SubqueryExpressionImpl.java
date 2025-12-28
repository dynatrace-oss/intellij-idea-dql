package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.SubqueryExpression;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

public abstract class SubqueryExpressionImpl extends ASTWrapperPsiElement implements SubqueryExpression {
    public SubqueryExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance(getProject()).calculateFieldName(
                "[",
                findChildByClass(DQLExpression.class),
                "]"
        );
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.subqueryExpression"), this, DQLIcon.DQL_SUBQUERY);
    }
}
