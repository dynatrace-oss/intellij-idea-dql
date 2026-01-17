package pl.thedeem.intellij.dpl.indexing;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLLexerAdapter;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;
import pl.thedeem.intellij.dpl.psi.DPLTokenSets;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

public class DPLFindUsagesProvider implements FindUsagesProvider {
    @Override
    public @Nullable WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(
                new DPLLexerAdapter(),
                DPLTokenSets.IDENTIFIERS,
                DPLTokenSets.COMMENTS,
                DPLTokenSets.STRING_LITERALS
        );
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiNamedElement;
    }

    @Override
    public @Nullable @NonNls String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public @Nls @NotNull String getType(@NotNull PsiElement psiElement) {
        return switch (psiElement) {
            case DPLFieldName ignored -> DPLBundle.message("findUsages.types.fields");
            case DPLVariable ignored -> DPLBundle.message("findUsages.types.variables");
            default -> DPLBundle.message("findUsages.types.unknown");
        };
    }

    @Override
    public @Nls @NotNull String getDescriptiveName(@NotNull PsiElement psiElement) {
        return getNodeText(psiElement, true);
    }

    @Override
    public @Nls @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (element instanceof PsiNamedElement named) {
            return StringUtil.defaultIfEmpty(named.getName(), named.getText());
        }
        return element.getText();
    }
}
