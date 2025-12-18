package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.definition.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Operator;
import pl.thedeem.intellij.dql.definition.model.Signature;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.impl.AbstractOperatorElementImpl;

public class UnsupportedOperatorDataTypeInspection extends BaseInspection {
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

                DQLParameterValueTypesValidator service = DQLParameterValueTypesValidator.getInstance(expression.getProject());

                for (MappedParameter parameter : operator.getParameters()) {
                    if (parameter.definition() != null) {
                        for (DQLParameterValueTypesValidator.ValueIssue issue : service.validate(parameter.holder(), parameter.definition())) {
                            if (doesNotContainErrorToken(issue.element())) {
                                holder.registerProblem(issue.element(), issue.issue());
                            }
                        }
                    }
                }
            }
        };
    }
}
