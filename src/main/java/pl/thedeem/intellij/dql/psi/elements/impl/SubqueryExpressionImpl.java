package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.SubqueryExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class SubqueryExpressionImpl extends ASTWrapperPsiElement implements SubqueryExpression {

    public SubqueryExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    public Set<DQLDataType> getDataType() {
        return Set.of(DQLDataType.SUBQUERY_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator()
                .addPart("[")
                .addPart(findChildByClass(DQLExpression.class))
                .addPart("]")
                .getFieldName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(DQLBundle.message("presentation.subqueryExpression"), this, DQLIcon.DQL_SUBQUERY);
    }
}
