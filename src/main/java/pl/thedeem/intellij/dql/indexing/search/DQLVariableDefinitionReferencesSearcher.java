package pl.thedeem.intellij.dql.indexing.search;

import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;

import java.util.List;

public final class DQLVariableDefinitionReferencesSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
    public DQLVariableDefinitionReferencesSearcher() {
        super(true);
    }

    @Override
    public void processQuery(@NotNull ReferencesSearch.SearchParameters parameters,
                             @NotNull Processor<? super PsiReference> consumer) {
        PsiElement target = parameters.getElementToSearch();
        if (!(target instanceof JsonProperty definition)) {
            return;
        }
        PsiFile file = definition.getContainingFile();
        if (file == null
                || file.getVirtualFile() == null
                || !DQLVariablesService.DQL_VARIABLES_FILE.equals(file.getVirtualFile().getName())) {
            return;
        }

        SearchScope scope = parameters.getScopeDeterminedByUser();
        List<DQLVariableExpression> usages = DQLVariablesService.getInstance(parameters.getProject()).findVariableUsages(definition);
        for (DQLVariableExpression usage : usages) {
            VirtualFile usageFile = usage.getContainingFile() == null ? null : usage.getContainingFile().getVirtualFile();
            if (usageFile != null && scope != null && !scope.contains(usageFile)) {
                continue;
            }
            for (PsiReference reference : usage.getReferences()) {
                if (!consumer.process(reference)) {
                    return;
                }
            }
        }
    }
}
