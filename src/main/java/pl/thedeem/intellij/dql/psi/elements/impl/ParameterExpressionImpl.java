package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.DQLBracketExpression;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterName;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;
import pl.thedeem.intellij.dql.psi.elements.ParameterExpression;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.model.MappedParameter;
import pl.thedeem.intellij.dql.services.query.DQLFieldsCalculatorService;

import java.util.Collection;
import java.util.Set;

public abstract class ParameterExpressionImpl extends ASTWrapperPsiElement implements ParameterExpression {
    private CachedValue<Parameter> definition;

    public ParameterExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        DQLParameterName parameter = this.findChildByClass(DQLParameterName.class);
        return parameter != null ? parameter.getName() : getText();
    }

    @Override
    public @NotNull Collection<String> getDataType() {
        DQLExpression expression = getExpression();
        return expression instanceof BaseTypedElement entity ? entity.getDataType() : Set.of();
    }

    @Override
    public boolean accessesData() {
        DQLExpression expression = getExpression();
        return expression instanceof BaseTypedElement entity && entity.accessesData();
    }

    private DQLExpression getExpression() {
        return findChildByClass(DQLExpression.class);
    }

    @Override
    public String getFieldName() {
        return DQLFieldsCalculatorService.getInstance().calculateFieldName(
                getName(),
                ": ",
                getExpression());
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.parameterExpression", getName()), this, DQLIcon.DQL_STATEMENT_PARAMETER);
    }

    @Override
    public @Nullable Parameter definition() {
        if (definition == null) {
            definition = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateDefinition(), this),
                    false
            );
        }
        return definition.getValue();
    }

    private @Nullable Parameter recalculateDefinition() {
        DQLParametersOwner parent = PsiUtils.getParentElement(this, (e) -> e instanceof DQLBracketExpression, DQLParametersOwner.class);
        if (parent == null) {
            return null;
        }
        MappedParameter parameter = parent.getParameter(this);
        if (parameter == null) {
            return null;
        }
        if (!StringUtil.equalsIgnoreCase(parameter.name(), this.getName())) {
            return null;
        }
        return parameter.definition();
    }
}
