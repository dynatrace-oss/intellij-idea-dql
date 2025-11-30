package pl.thedeem.intellij.dpl.completion;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import pl.thedeem.intellij.common.psi.PsiPatternUtils;
import pl.thedeem.intellij.dpl.psi.*;

public interface DPLPsiPatterns {
    ElementPattern<PsiElement> EMPTY_GROUP = PlatformPatterns.psiElement()
            .withParent(PlatformPatterns.psiElement(DPLTypes.COMMAND_KEYWORD))
            .withSuperParent(4,
                    PlatformPatterns.psiElement(DPLGroupExpression.class)
                            .with(PsiPatternUtils.withNumberOfChildren(1, PlatformPatterns.psiElement().whitespaceCommentEmptyOrError()))
            );

    ElementPattern<PsiElement> UNFINISHED_PARAMETERS_LIST = PlatformPatterns.psiElement()
            .withParent(PlatformPatterns.psiElement(DPLInvalidParameter.class))
            .withSuperParent(2, PlatformPatterns.psiElement(DPLConfiguration.class))
            .withSuperParent(3, PlatformPatterns.psiElement(DPLExpressionDefinition.class));

    ElementPattern<PsiElement> COMMAND = PlatformPatterns.or(
            PlatformPatterns.psiElement()
                    .withParent(PlatformPatterns.psiElement(DPLTypes.COMMAND_KEYWORD)),
            PlatformPatterns.psiElement()
                    .withParent(PlatformPatterns.psiElement(TokenType.ERROR_ELEMENT))
                    .withSuperParent(2, PlatformPatterns.psiElement(DPLTypes.ALTERNATIVE_GROUP_EXPRESSION))
    );

    ElementPattern<PsiElement> COMMAND_MATCHERS = PlatformPatterns.psiElement()
            .withParent(PlatformPatterns.psiElement(DPLTypes.COMMAND_KEYWORD))
            .withSuperParent(5, PlatformPatterns.psiElement(DPLTypes.COMMAND_MATCHERS));
}
