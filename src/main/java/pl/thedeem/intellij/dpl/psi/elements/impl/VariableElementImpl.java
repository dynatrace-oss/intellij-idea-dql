package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLElementFactory;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.impl.DPLSimpleExpressionImpl;
import pl.thedeem.intellij.dpl.indexing.references.DPLVariableReference;
import pl.thedeem.intellij.dpl.psi.DPLMacroDefinitionExpression;
import pl.thedeem.intellij.dpl.psi.DPLTypes;
import pl.thedeem.intellij.dpl.psi.DPLVariable;
import pl.thedeem.intellij.dpl.psi.elements.VariableElement;

import java.util.Objects;

public abstract class VariableElementImpl extends DPLSimpleExpressionImpl implements VariableElement {
    private CachedValue<DPLMacroDefinitionExpression> definition;

    public VariableElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable String getName() {
        PsiElement nameIdentifier = this.getNameIdentifier();
        return Objects.requireNonNullElse(nameIdentifier, this).getText();
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        ASTNode keyNode = this.getNode().findChildByType(DPLTypes.VARIABLE_NAME);
        if (keyNode != null) {
            return keyNode.getPsi();
        }
        return null;
    }

    @Override
    public PsiElement setName(@NotNull String newName) throws IncorrectOperationException {
        DPLVariable variable = DPLElementFactory.createVariable(newName, getProject());
        return this.replace(variable);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(getName(), this, DPLIcon.VARIABLE);
    }

    @Override
    public boolean isDefinition() {
        return this.getParent() instanceof DPLMacroDefinitionExpression;
    }

    @Override
    public @Nullable DPLMacroDefinitionExpression getDefinition() {
        if (definition == null) {
            definition = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateDefinition(), this),
                    false
            );
        }
        return definition.getValue();
    }

    private @Nullable DPLMacroDefinitionExpression recalculateDefinition() {
        if (this.isDefinition()) {
            return (DPLMacroDefinitionExpression) this.getParent();
        }

        PsiReference[] references = getReferences();
        for (PsiReference reference : references) {
            if (reference instanceof DPLVariableReference variableReference) {
                ResolveResult[] resolveResults = variableReference.multiResolve(true);
                if (resolveResults.length > 0 && resolveResults[0].getElement() instanceof DPLVariable def) {
                    return (DPLMacroDefinitionExpression) def.getParent();
                }
            }
        }
        return null;
    }
}
