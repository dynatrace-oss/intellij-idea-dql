package pl.thedeem.intellij.common.code;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexLiteralEscaper<T extends PsiLanguageInjectionHost> extends StringLiteralEscaper<T> {
    private final static Pattern POSIX_GROUP_PATTERN = Pattern.compile(":([^:]+):");
    private final Map<String, String> supportedPosixGroups;

    public RegexLiteralEscaper(@NotNull T host, Map<String, String> supportedPosixGroups) {
        super(host);
        this.supportedPosixGroups = supportedPosixGroups;
    }

    @Override
    public boolean decode(@NotNull TextRange textRange, @NotNull StringBuilder outChars) {
        String original = textRange.substring(myHost.getText());
        Matcher matcher = POSIX_GROUP_PATTERN.matcher(original);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1).toLowerCase();
            String replacement = supportedPosixGroups.getOrDefault(name, matcher.group(0));
            replacement = Matcher.quoteReplacement(replacement);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        outChars.append(result);
        return true;
    }

    @Override
    public int getOffsetInHost(int offsetInDecoded, @NotNull TextRange textRange) {
        String original = textRange.substring(myHost.getText());
        Matcher matcher = POSIX_GROUP_PATTERN.matcher(original);

        int decodedPos = 0;
        int originalPos = 0;

        while (matcher.find()) {
            int groupStart = matcher.start();
            int groupEnd = matcher.end();
            String name = matcher.group(1).toLowerCase();
            String originalMatchText = matcher.group(0);

            String replacement = supportedPosixGroups.getOrDefault(name, originalMatchText);
            int replacementLength = replacement.length();

            int unmatchedLength = groupStart - originalPos;
            if (offsetInDecoded < decodedPos + unmatchedLength) {
                return clamp(textRange.getStartOffset() + originalPos + (offsetInDecoded - decodedPos), textRange);
            }
            decodedPos += unmatchedLength;

            if (offsetInDecoded < decodedPos + replacementLength) {
                if (replacement.equals(originalMatchText)) {
                    return clamp(textRange.getStartOffset() + groupStart + (offsetInDecoded - decodedPos), textRange);
                }
                return clamp(textRange.getStartOffset() + groupStart, textRange);
            }

            decodedPos += replacementLength;
            originalPos = groupEnd;
        }
        return clamp(textRange.getStartOffset() + originalPos + (offsetInDecoded - decodedPos), textRange);
    }

    private int clamp(int value, TextRange range) {
        return Math.max(range.getStartOffset(), Math.min(value, range.getEndOffset()));
    }

    @Override
    public boolean isOneLine() {
        return true;
    }
}
