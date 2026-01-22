package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.definition.DPLDefinitionService;
import pl.thedeem.intellij.dpl.psi.elements.CharacterGroupContentElement;

import java.util.Map;

public abstract class CharacterGroupContentElementImpl extends ASTWrapperPsiElement implements CharacterGroupContentElement {
    private CachedValue<String> regex;

    public CharacterGroupContentElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable String getName() {
        return getText();
    }

    @Override
    public PsiElement setName(@NotNull String var1) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.characterClass"), this, DPLIcon.CHARACTER_GROUP);
    }

    @Override
    public @NotNull String getRegex() {
        if (regex == null) {
            regex = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateRegex(), this),
                    false
            );
        }
        return regex.getValue();
    }

    private String recalculateRegex() {
        DPLDefinitionService service = DPLDefinitionService.getInstance(getProject());

        String text = getText();
        Map<String, String> posixGroups = service.posixGroups();
        for (Map.Entry<String, String> group : posixGroups.entrySet()) {
            text = text.replaceAll(":" + group.getKey() + ":", group.getValue());
        }
        return "[" + text + "]";
    }
}
