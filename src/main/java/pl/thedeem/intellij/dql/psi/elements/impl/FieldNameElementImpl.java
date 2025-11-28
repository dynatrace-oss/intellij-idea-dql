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
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.indexing.ReferenceVariantsCalculator;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.FieldElement;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class FieldNameElementImpl extends ASTWrapperPsiElement implements FieldElement {
    private CachedValue<DQLAssignExpression> reference;
    private CachedValue<Set<DQLDataType>> dataType;
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
    public Set<DQLDataType> getDataType() {
        if (dataType == null) {
            dataType = CachedValuesManager.getManager(getProject()).createCachedValue(
                () -> new CachedValueProvider.Result<>(recalculateDataType(),
                        this
                ),
                false
            );
        }
        return dataType.getValue();
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
        return new DQLFieldNamesGenerator()
                .addPart(getName())
                .getFieldName();
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

    private DQLAssignExpression recalculateReference() {
        if (this instanceof DQLFieldExpression field) {
            ReferenceVariantsCalculator calculator = new ReferenceVariantsCalculator(field);
            return calculator.getAssignExpression();
        }
        return null;
    }

    private Set<DQLDataType> recalculateDataType() {
        if (!DQLSettings.getInstance().isCalculatingFieldsDataTypesEnabled()) {
            return Set.of(DQLDataType.IDENTIFIER, DQLDataType.ANY);
        }
        DQLAssignExpression definition = getAssignExpression();
        if (definition != null) {
            DQLExpression assignedValue = definition.getRightExpression();
            if (assignedValue instanceof BaseTypedElement value && value != this) {
                Set<DQLDataType> dataTypes = new HashSet<>(value.getDataType());
                dataTypes.add(DQLDataType.IDENTIFIER);
                return dataTypes;
            }
        }
        return Set.of(DQLDataType.ANY);
    }

    private DQLQuery recalculateParentQuery() {
       return PsiTreeUtil.getParentOfType(this, DQLQuery.class);
    }
}
