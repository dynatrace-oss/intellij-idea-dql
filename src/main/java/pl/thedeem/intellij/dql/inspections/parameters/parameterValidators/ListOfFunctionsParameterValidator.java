package pl.thedeem.intellij.dql.inspections.parameters.parameterValidators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;

import java.util.Set;

public class ListOfFunctionsParameterValidator implements ParameterValidator {
    private final Set<String> allowedFunctions;

    public ListOfFunctionsParameterValidator(@NotNull Set<String> functions) {
        this.allowedFunctions = functions;
    }

    @Override
    public boolean isElementInvalid(PsiElement element) {
        if (element instanceof DQLAssignExpression assignExpression) {
            element = assignExpression.getRightExpression();
        }
        if (element instanceof DQLFunctionCallExpression functionCall) {
            String name = functionCall.getName();
            return !allowedFunctions.contains(name);
        }
        return true;
    }
}
