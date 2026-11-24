package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.DQLElementFactory;
import pl.thedeem.intellij.dql.psi.DQLTypes;
import pl.thedeem.intellij.dql.psi.elements.VariableElement;
import pl.thedeem.intellij.dql.services.query.DQLFieldsCalculatorService;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public abstract class VariableElementImpl extends ASTWrapperPsiElement implements VariableElement {
    private CachedValue<DQLVariablesService.VariableDefinition> reference;

    public VariableElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        PsiElement nameIdentifier = this.getNameIdentifier();
        String text = Objects.requireNonNullElse(nameIdentifier, this).getText();
        return StringUtil.isNotEmpty(text) ? text.substring(1) : null;
    }

    @Override
    public PsiElement getNameIdentifier() {
        ASTNode keyNode = this.getNode().findChildByType(DQLTypes.IDENTIFIER);
        if (keyNode != null) {
            return keyNode.getPsi();
        }
        return this;
    }

    @Override
    public PsiElement setName(@NotNull String newName) {
        VariableElement variable = DQLElementFactory.createVariableElement(newName, getProject());
        PsiElement keyNode = getNameIdentifier();
        if (keyNode == null) {
            replace(variable);
        } else {
            keyNode.replace(Objects.requireNonNull(variable.getNameIdentifier()));
        }
        return this;
    }

    @Override
    public String getFieldName() {
        return DQLFieldsCalculatorService.getInstance().calculateFieldName(getName());
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.variable", this.getName()), this, DQLIcon.DQL_VARIABLE);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }

    @Override
    public boolean accessesData() {
        return false;
    }

    @Override
    public @NotNull Collection<String> getDataType() {
        DQLVariablesService.VariableDefinition definition = getDefinition();
        if (definition == null) {
            return Set.of();
        }
        return definition.dataTypes();
    }

    @Override
    public @Nullable String getValue() {
        DQLVariablesService.VariableDefinition definition = getDefinition();
        if (definition == null) {
            return null;
        }
        return definition.value();
    }

    private @Nullable DQLVariablesService.VariableDefinition getDefinition() {
        if (reference == null) {
            reference = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(
                            recalculateReference(),
                            PsiModificationTracker.MODIFICATION_COUNT,
                            DQLVariablesService.getInstance(getProject()).getUserDefinedVariablesTracker(getContainingFile())
                    ),
                    false
            );
        }
        return reference.getValue();
    }

    private @Nullable DQLVariablesService.VariableDefinition recalculateReference() {
        return DQLVariablesService.getInstance(getProject())
                .loadVariable(
                        getContainingFile(),
                        Objects.requireNonNullElse(getName(), "")
                );
    }
}
