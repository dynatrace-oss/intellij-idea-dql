package pl.thedeem.intellij.dql.inspections.parameters.parameterValidators;

import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;

import java.util.Set;

public class ParameterValueValidator implements ParameterValidator {
    private final Set<DQLDataType> dataTypes;

    public ParameterValueValidator(Set<DQLDataType> dataTypes) {
        this.dataTypes = dataTypes;
    }

    @Override
    public boolean isElementInvalid(PsiElement element) {
        if (element instanceof BaseElement entity) {
            Set<DQLDataType> returned = entity.getDataType();

            if (returned.contains(DQLDataType.ANY)) {
                return false;
            }

            return DQLDataType.doesNotSatisfy(returned, dataTypes);
        }
        return false;
    }
}
