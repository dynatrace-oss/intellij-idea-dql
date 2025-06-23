package pl.thedeem.intellij.dql.inspections.parameters;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

public class ConstantValueParameterInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);
                if (expression.getParent() instanceof DQLParametersOwner command) {
                    DQLParameterObject parameter = command.getParameter(expression);
                    if (parameter == null) {
                        return;
                    }
                    DQLParameterDefinition definition = parameter.getDefinition();
                    if (definition == null || definition.allowsDataAccess() || definition.isEnum()) {
                        return;
                    }
                    if (expression instanceof BaseElement element && element.accessesData()) {
                        holder.registerProblem(
                                expression,
                                DQLBundle.message(
                                        "inspection.parameter.constantValue.invalidValue",
                                        definition.name,
                                        DQLBundle.print(DQLDataType.getTypes(definition.getDQLTypes()))
                                )
                        );
                    }
                }
            }
        };
    }
}
