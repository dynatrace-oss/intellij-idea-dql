package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.definition.DPLDefinitionService;
import pl.thedeem.intellij.dpl.definition.model.Command;
import pl.thedeem.intellij.dpl.impl.DPLExpressionImpl;
import pl.thedeem.intellij.dpl.psi.DPLCommandKeyword;
import pl.thedeem.intellij.dpl.psi.elements.CommandElement;

import java.util.Objects;

public abstract class CommandElementImpl extends DPLExpressionImpl implements CommandElement {
    public CommandElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable String getName() {
        DPLCommandKeyword keyword = this.findChildByClass(DPLCommandKeyword.class);
        return keyword != null ? keyword.getName() : getText();
    }

    @Override
    public PsiElement setName(@NotNull String var1) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    public @Nullable Command getDefinition() {
        DPLDefinitionService service = DPLDefinitionService.getInstance(getProject());
        String name = Objects.requireNonNull(getName()).toUpperCase();
        return service.commands().get(name);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.expression"), this, DPLIcon.EXPRESSION);
    }
}
