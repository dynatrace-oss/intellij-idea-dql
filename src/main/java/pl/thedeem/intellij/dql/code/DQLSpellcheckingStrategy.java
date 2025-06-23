package pl.thedeem.intellij.dql.code;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.inspections.CommentSplitter;
import com.intellij.spellchecker.inspections.IdentifierSplitter;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionName;
import pl.thedeem.intellij.dql.psi.DQLString;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import org.jetbrains.annotations.NotNull;

public class DQLSpellcheckingStrategy extends SpellcheckingStrategy {
    @Override
    public @NotNull Tokenizer<?> getTokenizer(PsiElement element) {
        if (element instanceof PsiComment) {
            return new DQLCommentTokenizer();
        }

        if (element instanceof DQLFieldExpression) {
            return new DQLFieldTokenizer();
        }

        if (element instanceof DQLString) {
            return new DQLStringTokenizer();
        }

        if (element instanceof DQLVariableExpression) {
            return new DQLVariableTokenizer();
        }

        if (element instanceof DQLFunctionName) {
            return new DQLFunctionNameTokenizer();
        }

        return EMPTY_TOKENIZER;
    }

    private static class DQLCommentTokenizer extends Tokenizer<PsiComment> {
        @Override
        public void tokenize(@NotNull PsiComment element, @NotNull TokenConsumer consumer) {
            int startIndex = 0;
            for (char c : element.textToCharArray()) {
                if (c == '/' || Character.isWhitespace(c)) {
                    startIndex++;
                } else {
                    break;
                }
            }
            consumer.consumeToken(element, element.getText(), false, 0, TextRange.create(startIndex, element.getTextLength()), CommentSplitter.getInstance());
        }
    }

    private static class DQLFieldTokenizer extends Tokenizer<DQLFieldExpression> {
        public void tokenize(@NotNull DQLFieldExpression element, @NotNull TokenConsumer consumer) {
            String text = element.getText();
            consumer.consumeToken(element, text, true, 0, TextRange.allOf(text), IdentifierSplitter.getInstance());
        }
    }

    private static class DQLStringTokenizer extends Tokenizer<DQLString> {
        public void tokenize(@NotNull DQLString element, @NotNull TokenConsumer consumer) {
            String text = element.getText();
            consumer.consumeToken(element, text, true, 0, TextRange.allOf(text), PlainTextSplitter.getInstance());
        }
    }

    private static class DQLVariableTokenizer extends Tokenizer<DQLVariableExpression> {
        public void tokenize(@NotNull DQLVariableExpression element, @NotNull TokenConsumer consumer) {
            PsiElement identifier = element.getNameIdentifier();
            if (identifier != null) {
                String text = identifier.getText();
                consumer.consumeToken(identifier, text, true, 0, TextRange.allOf(text), IdentifierSplitter.getInstance());
            }
        }
    }

    private static class DQLFunctionNameTokenizer extends Tokenizer<DQLFunctionName> {
        public void tokenize(@NotNull DQLFunctionName element, @NotNull TokenConsumer consumer) {
            PsiElement identifier = element.getIdentifier();
            String text = identifier.getText();
            consumer.consumeToken(identifier, text, true, 0, TextRange.allOf(text), IdentifierSplitter.getInstance());
        }
    }
}
