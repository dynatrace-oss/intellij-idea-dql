package pl.thedeem.intellij.dpl.completion;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import pl.thedeem.intellij.dpl.psi.DPLGroupExpression;
import pl.thedeem.intellij.dpl.psi.DPLTypes;

public interface DPLPsiPatterns {
    ElementPattern<PsiElement> INSIDE_GROUP = PlatformPatterns.psiElement()
            .withParent(PlatformPatterns.psiElement(DPLTypes.COMMAND_KEYWORD))
            .withSuperParent(5, PlatformPatterns.psiElement(DPLGroupExpression.class));

    ElementPattern<PsiElement> COMMAND = PlatformPatterns.or(
            PlatformPatterns.psiElement()
                    .withParent(PlatformPatterns.psiElement(DPLTypes.COMMAND_KEYWORD)),
            PlatformPatterns.psiElement()
                    .withParent(PlatformPatterns.psiElement(TokenType.ERROR_ELEMENT))
    );

    ElementPattern<PsiElement> COMMAND_MATCHERS = PlatformPatterns.psiElement()
            .withParent(PlatformPatterns.psiElement(DPLTypes.COMMAND_KEYWORD))
            .withSuperParent(5, PlatformPatterns.psiElement(DPLTypes.MATCHERS_EXPRESSION));
}
