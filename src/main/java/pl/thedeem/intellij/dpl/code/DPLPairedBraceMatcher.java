package pl.thedeem.intellij.dpl.code;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.psi.DPLTypes;

public class DPLPairedBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = new BracePair[]{
            new BracePair(DPLTypes.L_PAREN, DPLTypes.R_PAREN, false),
            new BracePair(DPLTypes.L_BRACE, DPLTypes.R_BRACE, true),
            new BracePair(DPLTypes.L_BRACKET, DPLTypes.R_BRACKET, true),
            new BracePair(DPLTypes.L_ARROW, DPLTypes.R_ARROW, true),
    };

    @Override
    public BracePair @NotNull [] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType iElementType, @Nullable IElementType iElementType1) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile psiFile, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
