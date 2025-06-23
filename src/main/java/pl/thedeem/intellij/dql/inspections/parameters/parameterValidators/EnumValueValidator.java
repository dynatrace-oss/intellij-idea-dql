package pl.thedeem.intellij.dql.inspections.parameters.parameterValidators;

import com.intellij.psi.PsiElement;

import java.util.Collection;

public class EnumValueValidator implements ParameterValidator {
    private final Collection<String> enumValues;

    public EnumValueValidator(Collection<String> enumValues) {
        this.enumValues = enumValues;
    }

    @Override
    public boolean isElementInvalid(PsiElement element) {
        return !enumValues.contains(element.getText().trim());
    }
}
