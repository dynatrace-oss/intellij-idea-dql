package pl.thedeem.intellij.dql.indexing;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import pl.thedeem.intellij.dql.indexing.references.DQLFieldReference;
import pl.thedeem.intellij.dql.indexing.references.DQLFunctionReference;
import pl.thedeem.intellij.dql.indexing.references.DQLStatementKeywordReference;
import pl.thedeem.intellij.dql.indexing.references.DQLVariableReference;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionName;
import pl.thedeem.intellij.dql.psi.DQLQueryStatementKeyword;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import org.jetbrains.annotations.NotNull;

public class DQLReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(DQLFieldExpression.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        return new PsiReference[]{new DQLFieldReference((DQLFieldExpression) element)};
                    }
                });
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(DQLFunctionName.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        return new PsiReference[]{new DQLFunctionReference((DQLFunctionName) element)};
                    }
                });
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(DQLQueryStatementKeyword.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        return new PsiReference[]{new DQLStatementKeywordReference((DQLQueryStatementKeyword) element)};
                    }
                });
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(DQLVariableExpression.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        return new PsiReference[]{new DQLVariableReference((DQLVariableExpression) element)};
                    }
                });
    }
}