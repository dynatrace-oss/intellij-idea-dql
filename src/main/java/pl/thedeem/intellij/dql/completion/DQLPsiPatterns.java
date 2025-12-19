package pl.thedeem.intellij.dql.completion;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import pl.thedeem.intellij.common.psi.PsiPatternUtils;
import pl.thedeem.intellij.dql.psi.*;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public interface DQLPsiPatterns {
    PsiElementPattern.Capture<PsiElement> INSERTION_ERROR_ELEMENT = psiElement().withParent(psiElement(TokenType.ERROR_ELEMENT));

    PsiElementPattern.Capture<PsiElement> QUERY_KEYWORD = psiElement()
            .withParent(DQLCommandKeyword.class)
            .withSuperParent(2, psiElement(DQLCommand.class));

    PsiElementPattern.Capture<PsiElement> LAST_ELEMENT_IN_STATEMENT = psiElement()
            .withParent(psiElement(TokenType.ERROR_ELEMENT).with(PsiPatternUtils.isLastChildUntil(psiElement(DQLCommand.class))));

    ElementPattern<PsiElement> INSIDE_STATEMENT_PARAMETERS_LIST = PlatformPatterns.or(
            psiElement().withParent(DQLFieldExpression.class).withSuperParent(2, DQLCommand.class),
            psiElement().withParent(psiElement(TokenType.ERROR_ELEMENT).with(
                    PsiPatternUtils.isAfterElementSkipping(
                            psiElement().withParent(DQLCommand.class),
                            psiElement().whitespaceCommentEmptyOrError()
                    )
            ))
    );

    ElementPattern<PsiElement> SUGGEST_FIELD_VALUES = PlatformPatterns.or(
            psiElement().withParent(DQLFieldExpression.class)
    );
    ElementPattern<PsiElement> SUGGEST_FIELD_NAMES = PlatformPatterns.or(
            psiElement().withParent(DQLFieldName.class)
    );
    ElementPattern<PsiElement> SUGGEST_FUNCTION_PARAMETERS = psiElement().withSuperParent(2, DQLFunctionExpression.class);

    PsiElementPattern.Capture<PsiElement> SIBLING_OF_FIELD = psiElement()
            .withParent(
                    psiElement(TokenType.ERROR_ELEMENT).with(
                            PsiPatternUtils.isAfterElementSkipping(
                                    psiElement().with(PsiPatternUtils.isDeepNeighbourOf(psiElement(DQLFieldExpression.class))),
                                    psiElement().whitespaceCommentEmptyOrError()
                            )
                    )
            );

    PsiElementPattern.Capture<PsiElement> SUGGEST_QUERY_START = QUERY_KEYWORD.andNot(psiElement().withSuperParent(2, psiElement().afterSibling(psiElement())));

    ElementPattern<PsiElement> QUERY_COMMAND = PlatformPatterns.or(
            LAST_ELEMENT_IN_STATEMENT,
            psiElement().withParent(DQLCommandKeyword.class)
    );

    ElementPattern<PsiElement> SUGGEST_COMMAND_KEYWORDS = PlatformPatterns.or(
            QUERY_COMMAND,
            SUGGEST_QUERY_START
    );
}
