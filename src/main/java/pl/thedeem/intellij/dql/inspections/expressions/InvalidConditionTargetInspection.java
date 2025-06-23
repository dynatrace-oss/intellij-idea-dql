package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;

import java.util.Set;

public class InvalidConditionTargetInspection extends BaseInspection {
    public static final Set<Class<?>> ALLOWED_EXPRESSIONS = Set.of(
            DQLBoolean.class,
            DQLConditionExpression.class,
            DQLComparisonExpression.class,
            DQLEqualityExpression.class,
            DQLUnaryExpression.class,
            DQLVariableExpression.class,
            DQLFieldExpression.class,
            DQLFunctionCallExpression.class,
            DQLParenthesisedExpression.class,
            DQLArrayExpression.class,
            DQLInExpression.class
    );

    public static final Set<DQLDataType> ALLOWED_DATA_TYPES = Set.of(DQLDataType.BOOLEAN);

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitConditionExpression(@NotNull DQLConditionExpression expression) {
                super.visitConditionExpression(expression);
                for (DQLExpression operand : expression.getExpressionList()) {
                    validateExpressionOperands(holder, operand, ALLOWED_EXPRESSIONS);

                    if (operand instanceof BaseTypedElement element && returnValueDoesNotMatch(element, ALLOWED_DATA_TYPES)) {
                        holder.registerProblem(expression, DQLBundle.message(
                                "inspection.expression.operator.type.invalid",
                                DQLBundle.print(DQLDataType.getTypes(ALLOWED_DATA_TYPES))
                        ));
                    }
                }
            }
        };
    }
}
