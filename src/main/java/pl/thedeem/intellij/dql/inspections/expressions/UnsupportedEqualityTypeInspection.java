package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.DQLEqualityExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;
import pl.thedeem.intellij.dql.psi.elements.impl.ExpressionOperatorImpl;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.Map;
import java.util.Set;

public class UnsupportedEqualityTypeInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitEqualityExpression(@NotNull DQLEqualityExpression expression) {
                super.visitEqualityExpression(expression);
                Set<DQLDataType> dataType = expression.getDataType();
                if (dataType.contains(DQLDataType.FALSE)) {
                    Map<BaseElement, Set<DQLDataType>> invalidSides = findInvalidSidesForExpression(expression);
                    ExpressionOperatorImpl operator = expression.getOperator();
                    for (Map.Entry<BaseElement, Set<DQLDataType>> invalid : invalidSides.entrySet()) {
                        holder.registerProblem(invalid.getKey(), DQLBundle.message(
                                "inspection.equality.unsupportedType.invalidType",
                                operator != null ? operator.getText() : "",
                                DQLBundle.print(DQLDataType.getTypes(invalid.getValue())),
                                DQLBundle.print(DQLDataType.getTypes(invalid.getKey().getDataType()))
                        ));
                    }
                }
            }
        };
    }
}
