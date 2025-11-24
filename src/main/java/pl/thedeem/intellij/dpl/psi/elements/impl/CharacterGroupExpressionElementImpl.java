package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.definition.DPLDefinitionService;
import pl.thedeem.intellij.dpl.impl.DPLExpressionImpl;
import pl.thedeem.intellij.dpl.psi.elements.CharacterGroupExpressionElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CharacterGroupExpressionElementImpl extends DPLExpressionImpl implements CharacterGroupExpressionElement, PsiLanguageInjectionHost {
    public CharacterGroupExpressionElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isValidHost() {
        return true;
    }

    @Override
    public PsiLanguageInjectionHost updateText(@NotNull String s) {
        return this;
    }

    @Override
    public @NotNull LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        return new LiteralTextEscaper<>(this) {
            private final static Pattern POSIX_GROUP_PATTERN = Pattern.compile(":([^:]+):");

            @Override
            public boolean decode(@NotNull TextRange textRange, @NotNull StringBuilder outChars) {
                String original = textRange.substring(myHost.getText());
                Matcher matcher = POSIX_GROUP_PATTERN.matcher(original);
                DPLDefinitionService service = DPLDefinitionService.getInstance(getProject());

                StringBuilder result = new StringBuilder();
                while (matcher.find()) {
                    String name = matcher.group(1).toLowerCase();
                    String replacement = service.posixGroups().getOrDefault(name, matcher.group(0));
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
                DPLDefinitionService service = DPLDefinitionService.getInstance(getProject());

                while (matcher.find()) {
                    int groupStart = matcher.start();
                    int groupEnd = matcher.end();
                    String name = matcher.group(1).toLowerCase();
                    String originalMatchText = matcher.group(0);

                    String replacement = service.posixGroups().getOrDefault(name, originalMatchText);
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
        };
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.characterClass"), this, DPLIcon.EXPRESSION);
    }
}
