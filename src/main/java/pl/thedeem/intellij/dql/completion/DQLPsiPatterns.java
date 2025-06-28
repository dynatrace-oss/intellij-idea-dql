package pl.thedeem.intellij.dql.completion;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiJavaElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import pl.thedeem.intellij.dql.psi.*;

import static com.intellij.patterns.PsiJavaPatterns.psiElement;

public interface DQLPsiPatterns {
    PsiJavaElementPattern.Capture<PsiElement> QUERY_KEYWORD = psiElement()
            .withParent(DQLQueryStatementKeyword.class)
            .withSuperParent(2, psiElement(DQLQueryStatement.class));

    PsiJavaElementPattern.Capture<PsiElement> LAST_ELEMENT_IN_STATEMENT = psiElement()
            .withParent(psiElement(TokenType.ERROR_ELEMENT).with(AutocompleteUtils.isLastChildUntil(psiElement(DQLQueryStatement.class))));

    ElementPattern<PsiElement> INSIDE_STATEMENT_PARAMETERS_LIST = PlatformPatterns.or(
            psiElement().withParent(DQLFieldExpression.class).withSuperParent(2, DQLQueryStatement.class),
            psiElement().withParent(psiElement(TokenType.ERROR_ELEMENT).with(
                    AutocompleteUtils.isAfterElementSkipping(
                            psiElement(DQLParameterExpression.class).withParent(DQLQueryStatement.class),
                            psiElement().whitespaceCommentEmptyOrError()
                    )
            ))
    );

    ElementPattern<PsiElement> SUGGEST_FIELD_VALUES = PlatformPatterns.or(
            psiElement().withParent(DQLFieldExpression.class)
    );
    ElementPattern<PsiElement> SUGGEST_FUNCTION_PARAMETERS = psiElement().withSuperParent(2, DQLFunctionCallExpression.class);

    PsiJavaElementPattern.Capture<PsiElement> SIBLING_OF_FIELD = psiElement()
            .withParent(
                    psiElement(TokenType.ERROR_ELEMENT).with(
                            AutocompleteUtils.isAfterElementSkipping(
                                    psiElement().with(AutocompleteUtils.isDeepNeighbourOf(psiElement(DQLFieldExpression.class))),
                                    psiElement().whitespaceCommentEmptyOrError()
                            )
                    )
            );

    PsiJavaElementPattern.Capture<PsiElement> SUGGEST_QUERY_START = QUERY_KEYWORD.andNot(psiElement().withSuperParent(2, psiElement().afterSibling(psiElement())));

    ElementPattern<PsiElement> QUERY_COMMAND = PlatformPatterns.or(
            LAST_ELEMENT_IN_STATEMENT,
            psiElement().withParent(DQLQueryStatementKeyword.class)
    );
}
