package pl.thedeem.intellij.dql.definition.model;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum DQLDurationType {
    NANOSECOND(Set.of("ns")),
    MICROSECOND(Set.of("us")),
    MILLISECOND(Set.of("ms")),
    SECOND(Set.of("s", "sec", "secs", "second", "seconds")),
    MINUTE(Set.of("m", "min", "mins", "minute", "minutes")),
    HOUR(Set.of("h", "hr", "hrs", "hour", "hours")),
    DAY(Set.of("d", "day", "days")),
    WEEK(Set.of("w")),
    MONTH(Set.of("M")),
    QUARTER(Set.of("q")),
    YEAR(Set.of("y"));

    private final Set<String> types;

    DQLDurationType(Set<String> types) {
        this.types = types;
    }

    public static Set<String> getTypes() {
        return Arrays.stream(DQLDurationType.values())
                .flatMap(value -> value.types.stream())
                .collect(Collectors.toSet());
    }

    public static DQLDurationType getByType(String type) {
        if (type == null) {
            return null;
        }
        return Arrays.stream(DQLDurationType.values())
                .filter(value -> value.types.contains(type.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    public Instant getInstant(int duration, @NotNull Instant base) {
        return switch (this) {
            case NANOSECOND -> base.plus(Duration.ofNanos(duration));
            case MICROSECOND -> base.plus(Duration.ofNanos(duration * 1000L));
            case MILLISECOND -> base.plus(Duration.ofMillis(duration));
            case SECOND -> base.plus(Duration.ofSeconds(duration));
            case MINUTE -> base.plus(Duration.ofMinutes(duration));
            case HOUR -> base.plus(Duration.ofHours(duration));
            case DAY -> base.plus(Duration.ofDays(duration));
            case WEEK -> base.plus(Period.ofWeeks(duration));
            case MONTH -> base.plus(Period.ofMonths(duration));
            case QUARTER -> base.plus(Period.ofMonths(duration * 4));
            case YEAR -> base.plus(Period.ofYears(duration));
        };
    }
}
