package pl.thedeem.intellij.dql.indexing.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class DQLVariableReference extends PsiReferenceBase<DQLVariableExpression> {
    public DQLVariableReference(@NotNull DQLVariableExpression element) {
        super(element, TextRange.from(0, element.getTextLength()));
    }

    @Override
    public @Nullable PsiElement resolve() {
        return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        List<DQLVariableExpression> variables = DQLUtil.findVariablesInFile(myElement.getContainingFile());
        List<LookupElement> variants = new ArrayList<>();

        Set<String> alreadyVisited = new HashSet<>();
        for (DQLVariableExpression variable : variables) {
            if (alreadyVisited.add(variable.getName())) {
                variants.add(AutocompleteUtils.createLookupElement(
                        variable.getName(),
                        DQLIcon.DQL_VARIABLE,
                        AutocompleteUtils.VARIABLE,
                        null,
                        null
                ));
            }
        }
        return variants.toArray();
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        boolean isReference = super.isReferenceTo(element);
        if (!isReference) {
            if (element instanceof DQLVariableExpression variable) {
                return Objects.equals(myElement.getName(), variable.getName());
            }
            return false;
        }
        return true;
    }
}
