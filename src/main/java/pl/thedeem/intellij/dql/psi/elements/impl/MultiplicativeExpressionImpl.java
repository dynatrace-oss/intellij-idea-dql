package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.util.PsiTreeUtil;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.DQLMultiplicativeOperator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class MultiplicativeExpressionImpl extends ArithmeticalExpressionImpl {
    public MultiplicativeExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(DQLBundle.message("presentation.multiplicativeExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        List<DQLExpression> expressions = PsiTreeUtil.getChildrenOfTypeAsList(this, DQLExpression.class);
        DQLMultiplicativeOperator operator = PsiTreeUtil.getChildOfType(this, DQLMultiplicativeOperator.class);
        return new DQLFieldNamesGenerator()
                .addPart(expressions, operator)
                .getFieldName();
    }
}

