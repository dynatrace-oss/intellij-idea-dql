package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLOperationsLoader;
import pl.thedeem.intellij.dql.psi.elements.ArithmeticalExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class ArithmeticalExpressionImpl extends TwoSidesExpressionImpl implements ArithmeticalExpression {
  private CachedValue<Set<DQLDataType>> dataType;

  public ArithmeticalExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public Set<DQLDataType> getDataType() {
    if (dataType == null) {
      dataType = CachedValuesManager.getManager(getProject()).createCachedValue(
          () -> new CachedValueProvider.Result<>(recalculateDataType(), this),
          false
      );
    }
    return dataType.getValue();
  }

  @Override
  public boolean accessesData() {
    for (PsiElement child : getChildren()) {
      if (child instanceof BaseTypedElement entity && entity.accessesData()) {
        return true;
      }
    }
    return false;
  }

  private Set<DQLDataType> recalculateDataType() {
    Set<DQLDataType> dataType = new HashSet<>();
    dataType.add(DQLDataType.EXPRESSION);
    if (!DQLSettings.getInstance().isCalculatingExpressionDataTypesEnabled()) {
      dataType.addAll(Set.of(DQLDataType.LONG, DQLDataType.DURATION, DQLDataType.DOUBLE, DQLDataType.TIMESTAMP));
    }
    else {
      dataType.addAll(DQLOperationsLoader.getResultType(getOperator(), getLeftExpression(), getRightExpression()));
    }
    return Collections.unmodifiableSet(dataType);
  }
}
