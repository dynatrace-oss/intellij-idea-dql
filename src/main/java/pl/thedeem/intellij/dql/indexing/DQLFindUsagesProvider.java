package pl.thedeem.intellij.dql.indexing;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLLexerAdapter;
import pl.thedeem.intellij.dql.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DQLFindUsagesProvider implements FindUsagesProvider {
    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(
                new DQLLexerAdapter(),
                DQLTokenSets.IDENTIFIERS,
                DQLTokenSets.COMMENTS,
                DQLTokenSets.STRING_LITERALS
        );
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiNamedElement;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        return switch (element) {
            case DQLFieldExpression ignored -> DQLBundle.message("findUsages.types.fields");
            case DQLVariableExpression ignored -> DQLBundle.message("findUsages.types.variables");
            case DQLFunctionName ignored -> DQLBundle.message("findUsages.types.functions");
            case DQLParameterName ignored -> DQLBundle.message("findUsages.types.parameters");
            case DQLQueryStatementKeyword ignored -> DQLBundle.message("findUsages.types.statements");
            default -> DQLBundle.message("findUsages.types.unknown");
        };
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        return getNodeText(element, true);
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (element instanceof PsiNamedElement named) {
            return StringUtil.defaultIfEmpty(named.getName(), named.getText());
        }
        return element.getText();
    }
}
