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

public class InvalidTimeAlignmentTargetInspection extends BaseInspection {
    public static final Set<Class<?>> ALLOWED_EXPRESSIONS = Set.of(
            DQLDuration.class,
            DQLString.class,
            DQLParenthesisedExpression.class,
            DQLFunctionCallExpression.class,
            DQLTimeAlignmentNowExpression.class,
            DQLTimeAlignmentAtExpression.class,
            DQLFieldExpression.class,
            DQLArrayExpression.class
    );

    public static final Set<DQLDataType> ALLOWED_DATA_TYPES = Set.of(DQLDataType.TIMESTAMP, DQLDataType.DURATION);

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitTimeAlignmentAtExpression(@NotNull DQLTimeAlignmentAtExpression expression) {
                super.visitTimeAlignmentAtExpression(expression);
                validateExpressionOperands(holder, expression.getExpression(), ALLOWED_EXPRESSIONS);

                if (expression.getExpression() instanceof BaseTypedElement element && returnValueDoesNotMatch(element, ALLOWED_DATA_TYPES)) {
                    holder.registerProblem(expression, DQLBundle.message(
                            "inspection.expression.operator.type.invalid",
                            DQLBundle.print(DQLDataType.getTypes(ALLOWED_DATA_TYPES))
                    ));
                }
            }
        };
    }
}