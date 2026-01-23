package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.definition.DPLDefinitionService;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.impl.DPLDefinitionExpressionImpl;
import pl.thedeem.intellij.dpl.psi.*;
import pl.thedeem.intellij.dpl.psi.elements.GroupElement;

import java.util.ArrayList;
import java.util.List;

public abstract class GroupElementImpl extends DPLDefinitionExpressionImpl implements GroupElement {
    private CachedValue<ExpressionDescription> definition;

    public GroupElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.group"), this, DPLIcon.EXPRESSION);
    }

    @Override
    public @Nullable String getExpressionName() {
        return findChildByClass(DPLAlternativesExpression.class) != null ? "alternatives" : "sequence";
    }

    @Override
    public boolean isPotentiallyConfiguration(@Nullable PsiElement childToSkip) {
        if (this.findChildByClass(DPLAlternativesExpression.class) != null) {
            return false;
        }
        DPLExpressionsSequence sequence = PsiTreeUtil.getChildOfType(this, DPLExpressionsSequence.class);
        if (sequence == null) {
            return true;
        }

        for (DPLExpressionDefinition expression : sequence.getExpressionDefinitionList()) {
            DPLDefinitionExpression definedExpression = expression.getDefinedExpression();
            if (!(definedExpression instanceof DPLParameterExpression) && !(definedExpression instanceof DPLLiteralExpression)) {
                if (childToSkip == null || !PsiTreeUtil.isAncestor(expression, childToSkip, true)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public @NotNull List<DPLParameterExpression> getParameters() {
        if (PsiTreeUtil.getChildOfType(this, DPLAlternativesExpression.class) != null) {
            return List.of();
        }

        DPLExpressionsSequence sequence = PsiTreeUtil.getChildOfType(this, DPLExpressionsSequence.class);
        if (sequence == null) {
            return List.of();
        }

        List<DPLParameterExpression> result = new ArrayList<>();

        for (DPLExpressionDefinition expression : sequence.getExpressionDefinitionList()) {
            if (expression.getExpression() instanceof DPLParameterExpression parameter) {
                result.add(parameter);
            }
        }

        return result;
    }

    @Override
    public @Nullable ExpressionDescription getDefinition() {
        if (definition == null) {
            definition = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateDefinition(), this),
                    false
            );
        }
        return definition.getValue();
    }

    private @Nullable ExpressionDescription recalculateDefinition() {
        String expressionName = getExpressionName();
        if (expressionName == null) {
            return null;
        }
        DPLDefinitionService service = DPLDefinitionService.getInstance(getProject());
        return service.expressions().get(expressionName);
    }
}
