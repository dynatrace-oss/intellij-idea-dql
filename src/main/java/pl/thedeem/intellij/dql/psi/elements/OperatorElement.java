package pl.thedeem.intellij.dql.psi.elements;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.psi.elements.impl.ExpressionOperatorImpl;
import pl.thedeem.intellij.dql.services.definition.model.Operator;
import pl.thedeem.intellij.dql.services.definition.model.Signature;

import java.util.List;

public interface OperatorElement {
    @Nullable Operator getDefinition();

    @Nullable Signature getSignature();

    @NotNull List<PsiElement> getExpressions();

    @Nullable ExpressionOperatorImpl getOperator();

    @Nullable PsiElement getLeftExpression();

    @Nullable PsiElement getRightExpression();

    @Nullable String getOperatorSymbol();
}
