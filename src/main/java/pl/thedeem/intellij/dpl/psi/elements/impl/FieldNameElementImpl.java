package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLElementFactory;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;
import pl.thedeem.intellij.dpl.psi.DPLString;
import pl.thedeem.intellij.dpl.psi.elements.FieldNameElement;

import java.util.Objects;

public abstract class FieldNameElementImpl extends ASTWrapperPsiElement implements FieldNameElement {
    public FieldNameElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable String getName() {
        PsiElement nameIdentifier = this.getNameIdentifier();
        return Objects.requireNonNullElse(nameIdentifier, this).getText();
    }

    @Override
    public @NotNull String getExportName() {
        PsiElement identifier = this.getNameIdentifier();
        if (identifier instanceof DPLString string) {
            String text = string.getText();
            return text.substring(1, Math.max(text.length() - 1, 1));
        }
        return Objects.requireNonNullElse(getName(), "");
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        ASTNode node = this.getNode().getFirstChildNode();
        if (node != null) {
            return node.getPsi();
        }
        return this;
    }

    @Override
    public PsiElement setName(@NotNull String newName) throws IncorrectOperationException {
        DPLFieldName fieldName = DPLElementFactory.createFieldName(newName, getProject());
        return this.replace(fieldName);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(getName(), this, DPLIcon.FIELD);
    }
}
