package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.indexing.ReferenceVariantsCalculator;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLElementFactory;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLQuery;
import pl.thedeem.intellij.dql.psi.elements.FieldElement;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public abstract class FieldNameElementImpl extends ASTWrapperPsiElement implements FieldElement {
    private CachedValue<DQLAssignExpression> reference;
    private CachedValue<DQLQuery> parentQuery;

    public FieldNameElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return this.getText();
    }

    @Override
    public PsiElement getNameIdentifier() {
        ASTNode node = this.getNode().getFirstChildNode();
        if (node != null) {
            return node.getPsi();
        }
        return this;
    }

    @Override
    public @NotNull Collection<String> getDataType() {
        if (!DQLSettings.getInstance().isCalculatingFieldsDataTypesEnabled()) {
            return Set.of();
        }
        DQLAssignExpression assignedValue = getAssignExpression();
        if (assignedValue == getParent()) {
            return Set.of();
        }
        return assignedValue != null ? assignedValue.getDataType() : Set.of();
    }

    @Override
    public PsiElement setName(@NotNull String newName) {
        FieldElement fieldElement = DQLElementFactory.createFieldElement(newName, getProject());
        PsiElement keyNode = getNameIdentifier();
        if (keyNode == null) {
            replace(fieldElement);
        } else {
            keyNode.replace(Objects.requireNonNull(fieldElement.getNameIdentifier()));
        }
        return this;
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(getName(), this, DQLIcon.DQL_FIELD);
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance(getProject()).calculateFieldName(getName());
    }

    @Override
    public @Nullable DQLAssignExpression getAssignExpression() {
        if (reference == null) {
            reference = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateReference(), this),
                    false
            );
        }
        return reference.getValue();
    }

    @Override
    public @NotNull DQLQuery getParentQuery() {
        if (parentQuery == null) {
            parentQuery = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateParentQuery(), this),
                    false
            );
        }
        return parentQuery.getValue();
    }

    private @Nullable DQLAssignExpression recalculateReference() {
        if (this instanceof DQLFieldExpression field) {
            ReferenceVariantsCalculator calculator = new ReferenceVariantsCalculator(field);
            return calculator.getAssignExpression();
        }
        return null;
    }

    private DQLQuery recalculateParentQuery() {
        return PsiTreeUtil.getParentOfType(this, DQLQuery.class);
    }
}
