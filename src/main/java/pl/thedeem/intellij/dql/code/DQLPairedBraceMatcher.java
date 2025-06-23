package pl.thedeem.intellij.dql.code;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import pl.thedeem.intellij.dql.psi.DQLTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DQLPairedBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = new BracePair[]{
            new BracePair(DQLTypes.L_PARENTHESIS, DQLTypes.R_PARENTHESIS, false),
            new BracePair(DQLTypes.L_BRACE, DQLTypes.R_BRACE, true),
            new BracePair(DQLTypes.ARRAY_OPEN, DQLTypes.ARRAY_CLOSE, true),
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
