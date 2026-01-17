package pl.thedeem.intellij.dpl.style;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.psi.*;

import java.util.ArrayList;
import java.util.List;

public class DPLBlock extends AbstractBlock {
    private final SpacingBuilder spacingBuilder;
    private final Indent indent;
    private final DPLCodeStyleSettings settings;

    protected DPLBlock(
            @NotNull ASTNode node,
            @Nullable Wrap wrap,
            @Nullable Alignment alignment,
            SpacingBuilder spacingBuilder,
            Indent indent,
            DPLCodeStyleSettings settings
    ) {
        super(node, wrap, alignment);
        this.spacingBuilder = spacingBuilder;
        this.indent = indent;
        this.settings = settings;
    }

    @Override
    protected List<Block> buildChildren() {
        return getBlocks();
    }

    /**
     * This is the standard way of indenting children.
     */
    private List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<>();
        ASTNode child = myNode.getFirstChildNode();
        while (child != null) {
            if (child.getElementType() != TokenType.WHITE_SPACE) {
                blocks.add(new DPLBlock(
                        child,
                        getChildWrap(),
                        null,
                        spacingBuilder,
                        getChildIndent(child),
                        settings
                ));
            }
            child = child.getTreeNext();
        }
        return blocks;
    }

    private Indent getChildIndent(@NotNull ASTNode child) {
        IElementType childType = child.getElementType();
        PsiElement parent = this.getNode().getPsi();
        PsiElement element = child.getPsi();

        if (parent instanceof DPLExpressionsSequence) {
            return Indent.getNoneIndent();
        }
        if (parent instanceof DPLGroupExpression || parent instanceof DPLConfigurationExpression) {
            if (childType == DPLTypes.L_PAREN || childType == DPLTypes.R_PAREN) {
                return Indent.getNoneIndent();
            }
            return Indent.getNormalIndent();
        }
        if (parent instanceof DPLCharacterGroupExpression) {
            if (childType == DPLTypes.L_BRACKET || childType == DPLTypes.R_BRACKET) {
                return Indent.getNoneIndent();
            }
            return Indent.getNormalIndent();
        }
        if (parent instanceof DPLMatchersExpression) {
            if (childType == DPLTypes.L_BRACE || childType == DPLTypes.R_BRACE) {
                return Indent.getNoneIndent();
            }
            if (childType == DPLTypes.COMMAND_MATCHERS_CONTENT || isComment(childType)) {
                return Indent.getNormalIndent();
            }
            return Indent.getNoneIndent();
        }
        if (parent instanceof DPLLimitedQuantifier) {
            if (childType == DPLTypes.L_BRACE || childType == DPLTypes.R_BRACE) {
                return Indent.getNoneIndent();
            }
            if (element instanceof DPLLimitedQuantifierRanges || isComment(childType)) {
                return Indent.getNormalIndent();
            }
            return Indent.getNoneIndent();
        }
        if (parent instanceof DPLCommandMatchersContent) {
            return Indent.getNormalIndent();
        }
        if (parent instanceof DPLMacroDefinitionExpression) {
            if (childType == DPLTypes.SET) {
                return Indent.getNormalIndent();
            }
            if (element instanceof DPLExpressionsSequence) {
                return Indent.getContinuationIndent();
            }
        }
        if (parent instanceof DPLMetaExpression) {
            if (childType == DPLTypes.L_ARROW || childType == DPLTypes.R_ARROW) {
                return Indent.getNormalIndent();
            }
            if (childType == DPLTypes.META_EXPRESSION_CONTENT) {
                return Indent.getContinuationIndent();
            }
            return Indent.getNoneIndent();
        }
        if (parent instanceof DPLExportNameExpression) {
            if (childType == DPLTypes.COLON) {
                return Indent.getNormalIndent();
            }
            if (childType == DPLTypes.FIELD_NAME) {
                return Indent.getContinuationIndent();
            }
        }
        return Indent.getNoneIndent();
    }

    private static boolean isComment(IElementType childType) {
        return childType == DPLTypes.EOL_COMMENT || childType == DPLTypes.ML_COMMENT;
    }

    private Wrap getChildWrap() {
        IElementType elementType = this.getNode().getElementType();
        if (settings.WRAP_LONG_EXPRESSIONS) {
            if (DPLTypes.EXPRESSION == elementType || DPLTypes.EXPRESSIONS_SEQUENCE == elementType) {
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
}
