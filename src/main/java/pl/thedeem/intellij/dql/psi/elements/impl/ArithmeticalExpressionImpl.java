package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.definition.model.Signature;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.ArithmeticalExpression;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ArithmeticalExpressionImpl extends AbstractOperatorElementImpl implements ArithmeticalExpression {
    public ArithmeticalExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Collection<String> getDataType() {
        Signature signature = getSignature();
        if (signature == null) {
            return Set.of();
        }
        return signature.outputs();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.arithmeticalExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        List<DQLExpression> expressions = PsiTreeUtil.getChildrenOfTypeAsList(this, DQLExpression.class);
        ExpressionOperatorImpl operator = PsiTreeUtil.getChildOfType(this, ExpressionOperatorImpl.class);
        return new DQLFieldNamesGenerator()
                .addPart(expressions, operator)
                .getFieldName();
    }
}
