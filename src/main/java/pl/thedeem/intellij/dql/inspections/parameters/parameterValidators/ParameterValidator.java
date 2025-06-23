package pl.thedeem.intellij.dql.inspections.parameters.parameterValidators;

import com.intellij.psi.PsiElement;

public interface ParameterValidator {
    boolean isElementInvalid(PsiElement element);
}
