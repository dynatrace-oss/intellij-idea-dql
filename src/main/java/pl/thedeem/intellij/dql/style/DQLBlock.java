package pl.thedeem.intellij.dql.style;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLFile;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.impl.ExpressionOperatorImpl;

import java.util.ArrayList;
import java.util.List;

public class DQLBlock extends AbstractBlock {
    private final SpacingBuilder spacingBuilder;
    private final Indent indent;
    private final DQLCodeStyleSettings settings;

    protected DQLBlock(
            @NotNull ASTNode node,
            @Nullable Wrap wrap,
            @Nullable Alignment alignment,
            SpacingBuilder spacingBuilder,
            Indent indent,
            DQLCodeStyleSettings settings
    ) {
        super(node, wrap, alignment);
        this.spacingBuilder = spacingBuilder;
        this.indent = indent;
        this.settings = settings;
    }

    @Override
    protected List<Block> buildChildren() {
        return isExpressionWithOperands(getNode().getPsi()) ? getExpressionBlocks() : getBlocks();
    }

    /**
     * This is the standard way of indenting children.
     */
    private List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<>();
        ASTNode child = myNode.getFirstChildNode();
        while (child != null) {
            if (child.getElementType() != TokenType.WHITE_SPACE) {
                blocks.add(new DQLBlock(
                        child,
                        getChildWrap(child),
                        createAlignment(child),
                        spacingBuilder,
                        getChildIndent(child),
                        settings
                ));
            }
            child = child.getTreeNext();
        }
        return blocks;
    }

    /**
     * When indenting the expression, we assume that the first operand is not indented (as the whole expression is).
     * Afterward, the operator has normal indent, and all following expressions get continuation one.
     */
    private List<Block> getExpressionBlocks() {
        List<Block> blocks = new ArrayList<>();
        ASTNode child = myNode.getFirstChildNode();
        boolean firstExpression = true;
        while (child != null) {
            if (child.getElementType() != TokenType.WHITE_SPACE) {
                Indent indent;
                if (child.getPsi() instanceof DQLExpression) {
                    indent = firstExpression ? Indent.getNoneIndent() : Indent.getContinuationIndent();
                    firstExpression = false;
                } else if (child.getPsi() instanceof ExpressionOperatorImpl) {
                    indent = Indent.getNormalIndent();
                } else {
                    indent = getChildIndent(child);
                }
                blocks.add(new DQLBlock(
                        child,
                        getChildWrap(child),
                        createAlignment(child),
                        spacingBuilder,
                        indent,
                        settings
                ));
            }
            child = child.getTreeNext();
        }
        return blocks;
    }

    private Alignment createAlignment(@NotNull ASTNode child) {
        IElementType childType = child.getElementType();
        if (childType == DQLTypes.QUERY) {
            return Alignment.createChildAlignment(myAlignment);
        }
        return null;
    }

    private Indent getChildIndent(@NotNull ASTNode child) {
        IElementType childType = child.getElementType();
        PsiElement parent = this.getNode().getPsi();
        PsiElement element = child.getPsi();
        
        if (childType == DQLTypes.EOL_COMMENT || childType == DQLTypes.ML_COMMENT) {
            if (parent instanceof DQLFile || !settings.INDENT_BEFORE_PIPE) {
                return Indent.getNoneIndent();
            }
            return Indent.getNormalIndent();
        }
        if (childType == DQLTypes.COMMA || childType == DQLTypes.COLON) {
            return Indent.getNormalIndent();
        }
        // command-level
        if (element instanceof DQLQueryStatement command) {
            if (settings.INDENT_BEFORE_PIPE) {
                return command.isFirstStatement() ? Indent.getNoneIndent() : Indent.getNormalIndent();
            }
        }
        if (parent instanceof DQLQueryStatement command) {
            if (element instanceof DQLQueryStatementKeyword && !command.isFirstStatement()) {
                return Indent.getNormalIndent();
            }
            if (element instanceof DQLParameterExpression) {
                return Indent.getNormalIndent();
            }
            if (element instanceof DQLExpression) {
                return Indent.getContinuationIndent();
            }
        }
        // any kind of expression should be indented normally
        if (element instanceof DQLExpression) {
            return Indent.getNormalIndent();
        }
        // indent subqueries
        if (element instanceof DQLQuery && parent instanceof DQLSubqueryExpression) {
            return Indent.getContinuationIndent();
        }

        return Indent.getNoneIndent();
    }

    private Wrap getChildWrap(ASTNode child) {
        IElementType elementType = this.getNode().getElementType();
        IElementType childType = child.getElementType();
        if (DQLTypes.L_PARENTHESIS == childType && DQLTypes.FUNCTION_CALL_EXPRESSION == elementType) {
            return Wrap.createWrap(WrapType.NONE, true);
        }
        if (settings.WRAP_LONG_EXPRESSIONS) {
            if (DQLTypes.ARITHMETICAL_EXPRESSION == elementType
                    || DQLTypes.QUERY_STATEMENT == elementType
                    || DQLTypes.CONDITION_EXPRESSION == elementType
                    || DQLTypes.PARENTHESISED_EXPRESSION == elementType
                    || DQLTypes.FUNCTION_CALL_EXPRESSION == elementType
                    || DQLTypes.COMPARISON_EXPRESSION == elementType
                    || DQLTypes.ASSIGN_EXPRESSION == elementType
                    || DQLTypes.EQUALITY_EXPRESSION == elementType
            ) {
                return Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true);
            }
        }

        return Wrap.createWrap(WrapType.NONE, false);
    }

    @Override
    public Indent getIndent() {
        return indent;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return spacingBuilder.getSpacing(this, child1, child2);
    }

    @Override
    public boolean isLeaf() {
        return myNode.getFirstChildNode() == null;
    }

    @Override
    @NotNull
    public TextRange getTextRange() {
        return getNode().getTextRange();
    }

    private boolean isExpressionWithOperands(PsiElement element) {
        return element instanceof DQLArithmeticalExpression
                || element instanceof DQLUnaryExpression
                || element instanceof DQLComparisonExpression
                || element instanceof DQLConditionExpression
                || element instanceof DQLEqualityExpression
                || element instanceof DQLAssignExpression
                || element instanceof DQLInExpression
                || element instanceof DQLTimeAlignmentNowExpression
                || element instanceof DQLTimeAlignmentAtExpression
                || element instanceof DQLSortExpression;
    }
}
