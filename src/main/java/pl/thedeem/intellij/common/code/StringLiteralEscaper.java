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

        if (isTextBlock(fullText)) {
            out.append(injected);
            return true;
        }
        this.outSourceOffsets = new int[injected.length() + 1];
        CodeInsightUtilCore.parseStringCharacters(injected, out, outSourceOffsets, false);
        return true;
    }

    @Override
    public int getOffsetInHost(int offset, @NotNull TextRange textRange) {
        if (isTextBlock(myHost.getText())) {
            return textRange.getStartOffset() + offset;
        }
        int result = offset < this.outSourceOffsets.length ? this.outSourceOffsets[offset] : -1;
        return result == -1 ? -1 : Math.min(result, textRange.getLength()) + textRange.getStartOffset();
    }

    @Override
    public boolean isOneLine() {
        return false;
    }

    @Override
    public @NotNull TextRange getRelevantTextRange() {
        String text = this.myHost.getText();
        if (isTextBlock(myHost.getText())) {
            return TextRange.from(3, Math.max(3, text.length() - 3));
        }
        return TextRange.from(1, text.length() - 1);
    }

    private boolean isTextBlock(String text) {
        return (text.startsWith("\"\"\"") && text.endsWith("\"\"\""))
                || (text.startsWith("'''") && text.endsWith("'''"));
    }
}
