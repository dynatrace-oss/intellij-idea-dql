package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.DQLTypes;
import pl.thedeem.intellij.dql.psi.elements.BaseNamedElement;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

public abstract class ExpressionOperatorImpl extends ASTWrapperPsiElement implements BaseNamedElement {
    public ExpressionOperatorImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(getOperatorName(), this, DQLIcon.DQL_OPERATOR);
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance(getProject()).calculateFieldName(
                getText()
        );
    }

    public IElementType getNodeType() {
        return getFirstChild().getNode().getElementType();
    }

    @Override
    public boolean accessesData() {
        return false;
    }

    private String getOperatorName() {
        IElementType nodeType = getNodeType();
        if (DQLTypes.CONDITION_OPERATOR == nodeType) {
            return DQLBundle.message("expression.operator.condition");
        } else if (DQLTypes.EQUALITY_OPERATOR == nodeType) {
            return DQLBundle.message("expression.operator.equality");
        } else if (DQLTypes.COMPARISON_OPERATOR == nodeType) {
            return DQLBundle.message("expression.operator.comparison");
        } else if (DQLTypes.MULTIPLICATIVE_OPERATOR == nodeType) {
            return DQLBundle.message("expression.operator.multiplicative");
        } else if (DQLTypes.ADDITIVE_OPERATOR == nodeType) {
            return DQLBundle.message("expression.operator.additive");
        } else if (DQLTypes.UNARY_OPERATOR == nodeType) {
            return DQLBundle.message("expression.operator.unary");
        } else if (DQLTypes.ASSIGNMENT_OPERATOR == nodeType) {
            return DQLBundle.message("expression.operator.assignment");
        } else if (DQLTypes.IN_EXPRESSION_OPERATOR == nodeType) {
            return DQLBundle.message("expression.operator.in_expression_operand");
        } else if (DQLTypes.TIME_ALIGNMENT_EXPRESSION_OPERATOR == nodeType) {
            return DQLBundle.message("expression.operator.time_alignment_expression");
        }
        return DQLBundle.message("expression.operator.unknown");
    }
}
