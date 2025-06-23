package pl.thedeem.intellij.dql.sdk.model;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum DQLDurationType {
  NANOSECOND(Set.of("ns")),
  MILLISECOND(Set.of("ms")),
  SECOND(Set.of("s", "sec", "secs", "second", "seconds")),
  MINUTE(Set.of("m", "min", "mins", "minute", "minutes")),
  HOUR(Set.of("h", "hr", "hrs", "hour",  "hours")),
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
}
