package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLEqualityOperator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.EqualityExpression;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class EqualityExpressionImpl extends TwoSidesExpressionImpl implements EqualityExpression {
    private CachedValue<Set<DQLDataType>> dataType;

    public EqualityExpressionImpl(@NotNull ASTNode node) {
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
        PsiElement left = this.getFirstChild();
        if (left instanceof BaseTypedElement entity && entity.accessesData()) {
            return true;
        }
        PsiElement right = this.getLastChild();
        return right instanceof BaseTypedElement entity && entity.accessesData();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(DQLBundle.message("presentation.equalityExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        List<DQLExpression> expressions = PsiTreeUtil.getChildrenOfTypeAsList(this, DQLExpression.class);
        DQLEqualityOperator operator = PsiTreeUtil.getChildOfType(this, DQLEqualityOperator.class);
        return new DQLFieldNamesGenerator()
                .addPart(expressions, operator)
                .getFieldName();
    }

    private Set<DQLDataType> recalculateDataType() {
        Set<DQLDataType> dataType = new HashSet<>();
        dataType.add(DQLDataType.EXPRESSION);
        if (!DQLSettings.getInstance().isCalculatingExpressionDataTypesEnabled()) {
            dataType.add(DQLDataType.BOOLEAN);
        }
        else {
            dataType.addAll(DQLDefinitionService.getInstance(getProject()).getResultType(getOperator(), getLeftExpression(), getRightExpression()));
        }
        return Collections.unmodifiableSet(dataType);
    }
}
