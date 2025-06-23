package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.elements.impl.ExpressionOperatorImpl;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.DQLAdditiveExpression;
import pl.thedeem.intellij.dql.psi.DQLMultiplicativeExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.ArithmeticalExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;

import java.util.Map;
import java.util.Set;

public class UnsupportedArithmeticalTypeInspection extends BaseInspection {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new DQLVisitor() {
      @Override
      public void visitMultiplicativeExpression(@NotNull DQLMultiplicativeExpression expression) {
        super.visitMultiplicativeExpression(expression);
        validateArithmeticalType(holder, expression);
      }

      @Override
      public void visitAdditiveExpression(@NotNull DQLAdditiveExpression expression) {
        super.visitAdditiveExpression(expression);
        validateArithmeticalType(holder, expression);
      }
    };
  }

  private void validateArithmeticalType(@NotNull ProblemsHolder holder, @NotNull ArithmeticalExpression expression) {
    Set<DQLDataType> dataType = expression.getDataType();
    if (dataType.contains(DQLDataType.UNKNOWN)) {
      Map<BaseElement, Set<DQLDataType>> invalidSides = findInvalidSidesForExpression(expression);
      ExpressionOperatorImpl operator = expression.getOperator();
      for (Map.Entry<BaseElement, Set<DQLDataType>> invalid : invalidSides.entrySet()) {
        holder.registerProblem(invalid.getKey(), DQLBundle.message(
            "inspection.arithmetical.unsupportedType.invalidType",
                operator != null ? operator.getText() : "",
            DQLBundle.print(DQLDataType.getTypes(invalid.getValue())),
            DQLBundle.print(DQLDataType.getTypes(invalid.getKey().getDataType()))
        ));
      }
    }
  }
}
