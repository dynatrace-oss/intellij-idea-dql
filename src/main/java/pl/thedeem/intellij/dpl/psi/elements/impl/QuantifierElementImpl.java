package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.impl.DPLExpressionImpl;
import pl.thedeem.intellij.dpl.psi.*;
import pl.thedeem.intellij.dpl.psi.elements.QuantifierElement;

public abstract class QuantifierElementImpl extends DPLExpressionImpl implements QuantifierElement {
    public QuantifierElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.quantifier"), this, DPLIcon.QUANTIFIER);
    }

    @Override
    public @NotNull MinMaxValues getMinMaxValues() {
        long min = 0L;
        Long max = null;

        DPLLimitedQuantifier limited = PsiTreeUtil.getChildOfType(this, DPLLimitedQuantifier.class);
        DPLSimpleQuantifier simple = PsiTreeUtil.getChildOfType(this, DPLSimpleQuantifier.class);
        if (simple != null) {
            if (DPLTypes.ADD.equals(simple.getFirstChild().getNode().getElementType())) {
                min = 1L;
            }
        } else if (limited != null) {
            DPLLimitedQuantifierRanges ranges = limited.getLimitedQuantifierRanges();
            switch (ranges) {
                case DPLMinMaxQuantifier range -> {
                    min = range.getQuantifierLimitList().getFirst().getLongValue();
                    max = range.getQuantifierLimitList().getLast().getLongValue();
                }
                case DPLMinQuantifier range -> min = range.getQuantifierLimit().getLongValue();
                case DPLMaxQuantifier range -> max = range.getQuantifierLimit().getLongValue();
                case DPLExactQuantifier range -> min = max = range.getQuantifierLimit().getLongValue();
                case null, default -> {
                }
            }
        }
        return new MinMaxValues(min, max);
    }
}
