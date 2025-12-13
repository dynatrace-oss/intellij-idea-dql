package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.InExpression;

import java.util.List;
import java.util.Set;

public abstract class InExpressionImpl extends TwoSidesExpressionImpl implements InExpression {

    public InExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Set<String> getDataType() {
        return Set.of("dql.dataType.boolean");
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.inExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        List<DQLExpression> expressions = PsiTreeUtil.getChildrenOfTypeAsList(this, DQLExpression.class);
        return new DQLFieldNamesGenerator()
                .addPart(expressions, " in ")
                .getFieldName();
    }
}
