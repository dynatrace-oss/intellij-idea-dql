package pl.thedeem.intellij.dpl.code;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.inspections.IdentifierSplitter;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;
import pl.thedeem.intellij.dpl.psi.DPLString;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

public class DPLSpellcheckingStrategy extends SpellcheckingStrategy {
    @Override
    public @NotNull Tokenizer<?> getTokenizer(PsiElement element) {
        if (element instanceof DPLFieldName) {
            return new DPLFieldTokenizer();
        }

        if (element instanceof DPLString) {
            return new DPLStringTokenizer();
        }

        if (element instanceof DPLVariable) {
            return new DPLVariableTokenizer();
        }

        return EMPTY_TOKENIZER;
    }

    private static class DPLFieldTokenizer extends Tokenizer<DPLFieldName> {
        public void tokenize(@NotNull DPLFieldName element, @NotNull TokenConsumer consumer) {
            String text = element.getText();
            consumer.consumeToken(element, text, true, 0, TextRange.allOf(text), IdentifierSplitter.getInstance());
        }
    }

    private static class DPLStringTokenizer extends Tokenizer<DPLString> {
        public void tokenize(@NotNull DPLString element, @NotNull TokenConsumer consumer) {
            String text = element.getText();
            consumer.consumeToken(element, text, true, 0, TextRange.allOf(text), PlainTextSplitter.getInstance());
        }
    }

    private static class DPLVariableTokenizer extends Tokenizer<DPLVariable> {
        public void tokenize(@NotNull DPLVariable element, @NotNull TokenConsumer consumer) {
            String text = element.getText();
            consumer.consumeToken(element, text, true, 0, TextRange.allOf(text), IdentifierSplitter.getInstance());
        }
    }
}
