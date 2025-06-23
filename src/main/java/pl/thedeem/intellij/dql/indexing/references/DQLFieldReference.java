package pl.thedeem.intellij.dql.indexing.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.indexing.ReferenceVariantsCalculator;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class DQLFieldReference extends PsiPolyVariantReferenceBase<DQLFieldExpression> {
    private final String entityFieldName;

    public DQLFieldReference(@NotNull DQLFieldExpression element) {
        super(element, TextRange.from(0, element.getTextLength()));
        entityFieldName = element.getName();
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        List<DQLFieldExpression> fields = DQLUtil.findFieldsInFile(myElement.getContainingFile(), entityFieldName);
        List<ResolveResult> results = new ArrayList<>();
        for (DQLFieldExpression field : fields) {
            if (field.getParent() instanceof DQLAssignExpression) {
                results.add(new PsiElementResolveResult(field));
            }
        }
        return results.toArray(new ResolveResult[0]);
    }

    @Override
    public Object @NotNull [] getVariants() {
        ReferenceVariantsCalculator calculator = new ReferenceVariantsCalculator(myElement);
        Collection<ReferenceVariantsCalculator.VariantNode> variants = calculator.calculateVariants();

        List<LookupElement> result = new ArrayList<>();
        for (ReferenceVariantsCalculator.VariantNode value : variants) {
            LookupElementBuilder lookupElement = AutocompleteUtils.createLookupElement(
                    value.field.getText(),
                    DQLIcon.DQL_FIELD,
                    AutocompleteUtils.DATA_REFERENCE,
                    value.value,
                    null
            );
            lookupElement = lookupElement.withPresentableText(value.name);
            result.add(lookupElement);
        }

        return result.toArray();
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        boolean isReference = super.isReferenceTo(element);
        if (!isReference) {
            if (element instanceof DQLFieldExpression field) {
                return Objects.equals(myElement.getName(), field.getName());
            }
            return false;
        }
        return true;
    }
}
