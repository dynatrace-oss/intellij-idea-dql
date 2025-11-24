package pl.thedeem.intellij.dpl.indexing;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.indexing.references.DPLFieldNameReference;
import pl.thedeem.intellij.dpl.indexing.references.DPLVariableReference;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

public class DPLReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(DPLFieldName.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        return new PsiReference[]{new DPLFieldNameReference((DPLFieldName) element)};
                    }
                });
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(DPLVariable.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        return new PsiReference[]{new DPLVariableReference((DPLVariable) element)};
                    }
                });
    }
}
