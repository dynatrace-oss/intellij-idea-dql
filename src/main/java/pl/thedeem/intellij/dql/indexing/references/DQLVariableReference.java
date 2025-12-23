package pl.thedeem.intellij.dql.indexing.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.completion.CompletionUtils;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;

import java.util.*;

public final class DQLVariableReference extends PsiPolyVariantReferenceBase<DQLVariableExpression> {
    public DQLVariableReference(@NotNull DQLVariableExpression element) {
        super(element, TextRange.from(0, element.getTextLength()));
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        List<ResolveResult> results = new ArrayList<>();
        String name = getElement().getName();
        if (StringUtil.isNotEmpty(name)) {
            DQLVariablesService service = DQLVariablesService.getInstance(getElement().getProject());
            List<PsiElement> variableDefinitions = service.findVariableDefinitionFiles(name, getElement().getContainingFile());

            for (PsiElement variable : variableDefinitions) {
                results.add(new PsiElementResolveResult(variable));
            }
        }
        return results.toArray(new ResolveResult[0]);
    }

    @Override
    public Object @NotNull [] getVariants() {
        List<DQLVariableExpression> variables = DQLUtil.findVariablesInFile(myElement.getContainingFile());
        List<LookupElement> variants = new ArrayList<>();

        Set<String> alreadyVisited = new HashSet<>();
        for (DQLVariableExpression variable : variables) {
            if (alreadyVisited.add(variable.getName())) {
                variants.add(CompletionUtils.createLookupElement(
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
