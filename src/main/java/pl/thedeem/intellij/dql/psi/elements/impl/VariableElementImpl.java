package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLElementFactory;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.DQLTypes;
import pl.thedeem.intellij.dql.psi.elements.VariableElement;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class VariableElementImpl extends ASTWrapperPsiElement implements VariableElement {
    public VariableElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        PsiElement nameIdentifier = this.getNameIdentifier();
        return Objects.requireNonNullElse(nameIdentifier, this).getText();
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
        return new DQLFieldNamesGenerator().addPart(getName()).getFieldName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(this.getName(), this, DQLIcon.DQL_VARIABLE);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }

    @Override
    public boolean accessesData() {
        return false;
    }
}
