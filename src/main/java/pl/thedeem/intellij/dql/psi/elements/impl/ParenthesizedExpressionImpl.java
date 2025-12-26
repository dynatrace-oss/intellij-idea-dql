package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.ParenthesizedExpression;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

import java.util.Collection;
import java.util.Set;

public abstract class ParenthesizedExpressionImpl extends ASTWrapperPsiElement implements ParenthesizedExpression {
    private CachedValue<Collection<String>> dataType;

    public ParenthesizedExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Collection<String> getDataType() {
        if (dataType == null) {
            dataType = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateDataType(), this),
                    false
            );
        }
        return dataType.getValue();
    }

    @Override
    public boolean accessesData() {
        DQLExpression expression = this.findChildByClass(DQLExpression.class);
        return expression instanceof BaseTypedElement entity && entity.accessesData();
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance(getProject()).calculateFieldName(findChildByClass(DQLExpression.class));
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.parenthesisExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    private Collection<String> recalculateDataType() {
        PsiElement psiElement = DQLUtil.unpackParenthesis(this);

        if (psiElement instanceof BaseTypedElement dataField) {
            return dataField.getDataType();
        }
        return Set.of();
    }
}
