package pl.thedeem.intellij.dql.psi.elements;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.impl.ExpressionOperatorImpl;

import java.util.List;

public interface TwoSidesExpression {
    @Unmodifiable
    @NotNull List<DQLExpression> getExpressions();

    @Nullable ExpressionOperatorImpl getOperator();

    @Nullable DQLExpression getLeftExpression();

    @Nullable DQLExpression getRightExpression();
}
