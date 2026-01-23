package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.definition.DPLDefinitionService;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.impl.DPLSimpleExpressionImpl;
import pl.thedeem.intellij.dpl.psi.elements.VariableUsageExpressionElement;

public abstract class VariableUsageExpressionElementImpl extends DPLSimpleExpressionImpl implements VariableUsageExpressionElement {
    private CachedValue<ExpressionDescription> definition;

    public VariableUsageExpressionElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable String getName() {
        return getText();
    }

    @Override
    public @Nullable String getExpressionName() {
        return "variable";
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.expression"), this, DPLIcon.EXPRESSION);
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
