package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.elements.DurationElement;
import pl.thedeem.intellij.dql.sdk.model.DQLDurationType;

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
        return new StandardItemPresentation(getDurationRepresentation(type, numberPart), this, DQLIcon.DQL_NUMBER);
    }

    private String getDurationRepresentation(DQLDurationType type, Number numberPart) {
        if (type == null) {
            return DQLBundle.message("duration.unknown", numberPart);
        }
        boolean multi = numberPart.intValue() != 1;
        return switch (type) {
            case NANOSECOND -> DQLBundle.message(multi ? "duration.nanoseconds" : "duration.nanosecond", numberPart);
            case MICROSECOND -> DQLBundle.message(multi ? "duration.milliseconds" : "duration.millisecond", numberPart);
            case MILLISECOND -> DQLBundle.message(multi ? "duration.microseconds" : "duration.microsecond", numberPart);
            case SECOND -> DQLBundle.message(multi ? "duration.seconds" : "duration.second", numberPart);
            case MINUTE -> DQLBundle.message(multi ? "duration.minutes" : "duration.minute", numberPart);
            case HOUR -> DQLBundle.message(multi ? "duration.hours" : "duration.hour", numberPart);
            case DAY -> DQLBundle.message(multi ? "duration.days" : "duration.day", numberPart);
            case WEEK -> DQLBundle.message(multi ? "duration.weeks" : "duration.week", numberPart);
            case MONTH -> DQLBundle.message(multi ? "duration.months" : "duration.month", numberPart);
            case QUARTER -> DQLBundle.message(multi ? "duration.quarters" : "duration.quarter", numberPart);
            case YEAR -> DQLBundle.message(multi ? "duration.years" : "duration.year", numberPart);
        };
    }

    @Override
    public @NotNull Set<String> getDataType() {
        return Set.of("dql.dataType.duration");
    }

    @Override
    public boolean accessesData() {
        return false;
    }
}
