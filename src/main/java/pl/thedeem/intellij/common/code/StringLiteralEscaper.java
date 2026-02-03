package pl.thedeem.intellij.common.code;

import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;

public class StringLiteralEscaper<T extends PsiLanguageInjectionHost> extends LiteralTextEscaper<T> {
    private int[] outSourceOffsets;

    public StringLiteralEscaper(@NotNull T host) {
        super(host);
    }

    @Override
    public boolean decode(@NotNull TextRange textRange, @NotNull StringBuilder out) {
        String fullText = myHost.getText();
        String injected = textRange.substring(fullText);

        if (isEscapingDisabled()) {
            out.append(injected);
            return true;
        }
        this.outSourceOffsets = new int[injected.length() + 1];
        return CodeInsightUtilCore.parseStringCharacters(injected, out, outSourceOffsets, false);
    }

    @Override
    public int getOffsetInHost(int offset, @NotNull TextRange textRange) {
        if (isEscapingDisabled()) {
            return textRange.getStartOffset() + offset;
        }
        if (offset >= this.outSourceOffsets.length) {
            return -1;
        }
        return Math.min(this.outSourceOffsets[offset], textRange.getLength()) + textRange.getStartOffset();
    }

    @Override
    public boolean isOneLine() {
        return false;
    }

    @Override
    public @NotNull TextRange getRelevantTextRange() {
        if (myHost instanceof InjectedLanguageHolder holder) {
            return holder.getHostTextRange();
        }
        return TextRange.from(1, Math.max(1, myHost.getTextLength() - 1));
    }

    private boolean isEscapingDisabled() {
        if (myHost instanceof InjectedLanguageHolder holder) {
            return holder.isEscapingDisabled();
        }
        return false;
    }
}
