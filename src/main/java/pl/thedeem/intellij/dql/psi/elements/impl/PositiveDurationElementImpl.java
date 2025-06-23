package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.sdk.model.DQLDurationType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.DurationElement;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PositiveDurationElementImpl extends ASTWrapperPsiElement implements DurationElement {
    private final static Pattern DURATION_PATTERN = Pattern.compile("-?(\\d+)(\\w+)");

    public PositiveDurationElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public Number getNumberPart() {
        String text = getText();
        if (text != null) {
            Matcher matcher = DURATION_PATTERN.matcher(text.trim());
            if (matcher.matches()) {
                return Integer.valueOf(matcher.group(1));
            }
        }
        return null;
    }

    @Override
    public DQLDurationType getDurationType() {
        String text = getText();
        if (text != null) {
            Matcher matcher = DURATION_PATTERN.matcher(text.trim());
            if (matcher.matches()) {
                return DQLDurationType.getByType(matcher.group(2));
            }
        }

        return null;
    }

    @Override
    public String getName() {
       return getText();
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator().addPart(getName()).getFieldName();
    }

    @Override
    public ItemPresentation getPresentation() {
        DQLDurationType type = getDurationType();
        Number numberPart = getNumberPart();
        String key = type != null ? (type.name().toLowerCase() + (numberPart.intValue() != 1 ? "s" : "")) : "unknown";
        String name = DQLBundle.message(
                "documentation.duration.name", numberPart,
                DQLBundle.message("duration." + key)
        );
        return new DQLItemPresentation(name, this, DQLIcon.DQL_NUMBER);
    }

    @Override
    public Set<DQLDataType> getDataType() {
        return Set.of(DQLDataType.POSITIVE_DURATION);
    }

    @Override
    public boolean accessesData() {
        return false;
    }
}
