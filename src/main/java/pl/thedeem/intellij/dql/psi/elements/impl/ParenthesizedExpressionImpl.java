package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.ParenthesizedExpression;

import java.util.Set;

public abstract class ParenthesizedExpressionImpl extends ASTWrapperPsiElement implements ParenthesizedExpression {
    private CachedValue<Set<DQLDataType>> dataType;

    public ParenthesizedExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public Set<DQLDataType> getDataType() {
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
        return new DQLFieldNamesGenerator().addPart(findChildByClass(DQLExpression.class)).getFieldName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(DQLBundle.message("presentation.parenthesisExpression"), this, DQLIcon.DQL_EXPRESSION);
    }
    private Set<DQLDataType> recalculateDataType() {
        PsiElement psiElement = DQLUtil.unpackParenthesis(this);

        if (psiElement instanceof BaseTypedElement dataField) {
            return dataField.getDataType();
        }
        return Set.of(DQLDataType.ANY);
    }
}
