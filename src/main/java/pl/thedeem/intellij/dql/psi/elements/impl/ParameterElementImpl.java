package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.elements.ParameterElement;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

public abstract class ParameterElementImpl extends ASTWrapperPsiElement implements ParameterElement {
    public ParameterElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return getText().trim();
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance().calculateFieldName(getName());
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(this.getName(), this, DQLIcon.DQL_STATEMENT_PARAMETER);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }
}
