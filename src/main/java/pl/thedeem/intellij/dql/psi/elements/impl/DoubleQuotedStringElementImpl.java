package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.StringElement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class DoubleQuotedStringElementImpl extends ASTWrapperPsiElement implements StringElement {
    private static final Pattern TIMESTAMP_START = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}");
    private CachedValue<Set<DQLDataType>> dataType;

    public DoubleQuotedStringElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator().addPart(getText()).getFieldName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(this.getName(), this, DQLIcon.DQL_STRING);
    }

    @Override
    public Set<DQLDataType> getDataType() {
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
        return new TextRange(1, getTextLength() - 1);
    }

    private Set<DQLDataType> recalculateDataType() {
        Set<DQLDataType> dataType = new HashSet<>();
        dataType.add(DQLDataType.STRING);
        if (isTimestampString()) {
            dataType.add(DQLDataType.TIMESTAMP);
        }
        return Collections.unmodifiableSet(dataType);
    }

    private boolean isTimestampString() {
        String content = getContent().trim();
        return TIMESTAMP_START.matcher(content).find();
    }
}
