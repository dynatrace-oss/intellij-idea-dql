package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.Operator;
import pl.thedeem.intellij.dql.definition.model.Signature;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.impl.AbstractOperatorElementImpl;
import pl.thedeem.intellij.dql.psi.elements.impl.ExpressionOperatorImpl;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class UnsupportedOperatorDataTypeInspection extends BaseInspection {
    private final static Set<String> IGNORED_TYPES = Set.of("dql.dataType.null", "dql.dataType.array");

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);

                if (!(expression instanceof AbstractOperatorElementImpl operator)) {
                    return;
                }

                Signature signature = operator.getSignature();
                Operator definition = operator.getDefinition();
                if (signature == null || definition == null) {
                    return;
                }

                int i = 0;
                for (PsiElement child : operator.getChildren()) {
                    if (child instanceof BaseTypedElement && !(child instanceof ExpressionOperatorImpl)) {
                        validateExpression(definition, child, signature, i, holder);
                        i++;
                    }
                }
            }
        };
    }

    private void validateExpression(
            @NotNull Operator operator,
            @NotNull PsiElement expression,
            @NotNull Signature signature,
            int parameterIndex,
            @NotNull ProblemsHolder holder) {
        Collection<String> actual = expression instanceof BaseTypedElement typed ? typed.getDataType() : Set.of();
        Collection<String> definition = signature.parameters().size() > parameterIndex ?
                Objects.requireNonNullElse(signature.parameters().get(parameterIndex).valueTypes(), List.of()) : List.of();

        if (definition.isEmpty() || actual.isEmpty()) {
            return;
        }
        if (actual.stream().anyMatch(IGNORED_TYPES::contains) || definition.stream().anyMatch(IGNORED_TYPES::contains)) {
            return;
        }
        if (definition.stream().noneMatch(actual::contains)) {
            holder.registerProblem(expression,
                    DQLBundle.message(
                            "inspection.operator.unsupportedType.invalidType",
                            operator.symbol(),
                            DQLBundle.types(definition, expression.getProject()),
                            DQLBundle.types(actual, expression.getProject())
                    ));
        }
    }
}
