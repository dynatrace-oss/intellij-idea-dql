package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.elements.FunctionNameElement;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

public abstract class FunctionNameElementImpl extends ASTWrapperPsiElement implements FunctionNameElement {
    public FunctionNameElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return getText().trim();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(getName(), this, DQLIcon.DQL_FUNCTION);
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance().calculateFieldName(getName());
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }
}
