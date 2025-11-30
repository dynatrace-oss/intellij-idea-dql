package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.common.code.RegexLiteralEscaper;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.definition.DPLDefinitionService;
import pl.thedeem.intellij.dpl.impl.DPLExpressionImpl;
import pl.thedeem.intellij.dpl.psi.elements.CharacterGroupExpressionElement;

public abstract class CharacterGroupExpressionElementImpl extends DPLExpressionImpl implements CharacterGroupExpressionElement, PsiLanguageInjectionHost {
    public CharacterGroupExpressionElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isValidHost() {
        return true;
    }

    @Override
    public PsiLanguageInjectionHost updateText(@NotNull String s) {
        return this;
    }

    @Override
    public @NotNull LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        DPLDefinitionService service = DPLDefinitionService.getInstance(getProject());
        return new RegexLiteralEscaper<>(this, service.posixGroups());
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.characterClass"), this, DPLIcon.EXPRESSION);
    }
}
