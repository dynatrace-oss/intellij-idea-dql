package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.common.code.StringLiteralEscaper;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.elements.StringElement;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class DoubleQuotedStringElementImpl extends ASTWrapperPsiElement implements StringElement {
    private static final Pattern TIMEFRAME_STRING = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}[^/]+/\\d{4}-\\d{2}-\\d{2}[^/]+");
    private static final Pattern TIMESTAMP_STRING = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}");
    private CachedValue<Set<String>> dataType;

    public DoubleQuotedStringElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance(getProject()).calculateFieldName(getName());
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(this.getName(), this, DQLIcon.DQL_STRING);
    }

    @Override
    public @NotNull Set<String> getDataType() {
        if (dataType == null) {
            dataType = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateDataType(), this),
                    false
            );
        }
        return dataType.getValue();
    }

    @Override
    public boolean accessesData() {
        return false;
    }

    @Override
    public String getContent() {
        String text = getText();
        TextRange hostTextRange = getHostTextRange();
        return text.substring(hostTextRange.getStartOffset(), hostTextRange.getEndOffset());
    }

    @Override
    public TextRange getHostTextRange() {
        return new TextRange(1, Math.max(1, getTextLength() - 1));
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
        return new StringLiteralEscaper<>(this);
    }

    private Set<String> recalculateDataType() {
        Set<String> dataType = new HashSet<>();
        dataType.add("dql.dataType.string");

        String content = getContent().trim();
        if (TIMEFRAME_STRING.matcher(content).matches()) {
            dataType.add("dql.dataType.timeframe");
        } else if (TIMESTAMP_STRING.matcher(content).matches()) {
            dataType.add("dql.dataType.timestamp");
        }
        return Collections.unmodifiableSet(dataType);
    }
}
