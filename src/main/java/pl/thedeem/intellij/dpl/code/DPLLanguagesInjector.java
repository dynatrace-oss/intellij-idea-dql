package pl.thedeem.intellij.dpl.code;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.psi.DPLCharacterGroupExpression;

import java.util.List;

public class DPLLanguagesInjector implements MultiHostInjector {
    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        Language regex = Language.findLanguageByID("RegExp");
        if (regex != null && context instanceof DPLCharacterGroupExpression rExp && rExp instanceof PsiLanguageInjectionHost host && rExp.getCharacterGroupContent() != null) {
            registrar.startInjecting(regex)
                    .addPlace("[", "]", host, rExp.getCharacterGroupContent().getTextRange().shiftLeft(rExp.getTextRange().getStartOffset()))
                    .doneInjecting();
        }
    }

    @Override
    public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return List.of(DPLCharacterGroupExpression.class);
    }
}
