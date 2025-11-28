package pl.thedeem.intellij.dpl.indexing.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLUtil;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DPLFieldNameReference extends PsiPolyVariantReferenceBase<DPLFieldName> {
    private final String fieldName;

    public DPLFieldNameReference(@NotNull DPLFieldName element) {
        super(element, TextRange.from(0, element.getTextLength()));
        this.fieldName = element.getExportName();
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean b) {
        List<DPLFieldName> fields = DPLUtil.findFields(myElement.getContainingFile(), fieldName);
        List<ResolveResult> results = new ArrayList<>();
        for (DPLFieldName field : fields) {
            results.add(new PsiElementResolveResult(field));
        }
        return results.toArray(new ResolveResult[0]);
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        boolean isReference = super.isReferenceTo(element);
        if (!isReference) {
            if (element instanceof DPLFieldName field) {
                return Objects.equals(fieldName, field.getExportName());
            }
            return false;
        }
        return true;
    }
}
