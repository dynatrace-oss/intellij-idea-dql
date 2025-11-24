package pl.thedeem.intellij.dpl.indexing.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.completion.CompletionUtils;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.DPLUtil;
import pl.thedeem.intellij.dpl.completion.DPLCompletions;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

import java.util.*;

public class DPLVariableReference extends PsiPolyVariantReferenceBase<DPLVariable> {
    private final String name;

    public DPLVariableReference(@NotNull DPLVariable element) {
        super(element, TextRange.from(0, element.getTextLength()));
        this.name = Objects.requireNonNullElse(element.getName(), "");
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        List<DPLVariable> variables = DPLUtil.findVariables(myElement.getContainingFile(), name);
        List<ResolveResult> results = new ArrayList<>();
        for (DPLVariable variable : variables) {
            if (variable.isDefinition()) {
                results.add(new PsiElementResolveResult(variable));
            }
        }
        return results.toArray(new ResolveResult[0]);
    }

    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElement> variants = new ArrayList<>();
        for (DPLVariable variable : DPLUtil.findVariables(myElement.getContainingFile())) {
            if (variable.isDefinition()) {
                variants.add(CompletionUtils.createLookupElement(
                        variable.getName(),
                        DPLIcon.VARIABLE,
                        DPLCompletions.VARIABLE,
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
            if (element instanceof DPLVariable variable) {
                return Objects.equals(myElement.getName(), variable.getName());
            }
            return false;
        }
        return true;
    }
}
