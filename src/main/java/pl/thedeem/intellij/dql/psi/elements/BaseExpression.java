package pl.thedeem.intellij.dql.psi.elements;

import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.List;
import java.util.Set;

public interface BaseExpression {
    List<Operand> getOperands();
    boolean checkForStaticValues();

    record Operand(PsiElement operand, Set<Class<?>> validTargets, Set<DQLDataType> validTypes){}

    String getFieldName();
}
