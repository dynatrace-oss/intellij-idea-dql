package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.common.code.InjectedLanguageHolder;
import pl.thedeem.intellij.common.code.RegexLiteralEscaper;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.definition.DPLDefinitionService;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.impl.DPLDefinitionExpressionImpl;
import pl.thedeem.intellij.dpl.psi.elements.CharacterGroupExpressionElement;

public abstract class CharacterGroupExpressionElementImpl extends DPLDefinitionExpressionImpl implements CharacterGroupExpressionElement, PsiLanguageInjectionHost, InjectedLanguageHolder {
    private CachedValue<ExpressionDescription> definition;

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
    public @Nullable String getExpressionName() {
        return "character_class";
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.characterClass"), this, DPLIcon.EXPRESSION);
    }

    @Override
    public @Nullable ExpressionDescription getDefinition() {
        if (definition == null) {
            definition = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateDefinition(), this),
                    false
            );
        }
        return definition.getValue();
    }

    private @Nullable ExpressionDescription recalculateDefinition() {
        String expressionName = getExpressionName();
        if (expressionName == null) {
            return null;
        }
        DPLDefinitionService service = DPLDefinitionService.getInstance(getProject());
        return service.expressions().get(expressionName);
    }

    @Override
    public TextRange getHostTextRange() {
        return new TextRange(1, Math.max(1, getTextLength() - 1));
    }
}
