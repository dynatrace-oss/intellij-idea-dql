package pl.thedeem.intellij.dql.sdk.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.DQLFunctionGroup;

import java.util.*;
import java.util.stream.Collectors;

public enum DQLDataType {
  NULL("null"),
  UNKNOWN("unknown"),
  ANY("any"),

  TIMESTAMP("timestamp"),
  STRING("string"),
  NUMBER("number"),
  LONG("long", NUMBER),
  POSITIVE_LONG("positive long", LONG),
  NEGATIVE_LONG("negative long", LONG),
  DOUBLE("double", NUMBER),
  POSITIVE_DOUBLE("positive double", DOUBLE),
  NEGATIVE_DOUBLE("negative double", DOUBLE),

  DURATION("duration"),
  POSITIVE_DURATION("positive duration", DURATION),
  NEGATIVE_DURATION("negative duration", DURATION),
  BOOLEAN("boolean"),
  TRUE("true", BOOLEAN),
  FALSE("false", BOOLEAN),
  DATA_OBJECT("data object"),
  UID("uid"),
  IP("ip"),
  IP_ADDRESS("ip address", IP),
  COUNTER("counter"),
  BINARY("binary"),
  RECORD("record"),
  PARSED_RECORD("parsed_record", RECORD),
  TIMEFRAME("timeframe"),
  ARRAY("array"),

  EXPRESSION("expression"),
  JOIN_CONDITION("join condition", EXPRESSION),
  SUBQUERY_EXPRESSION("subquery expression", EXPRESSION),
  NAMED_SUBQUERY_EXPRESSION("named subquery expression", EXPRESSION),
  AGGREGATION_FUNCTION("aggregation function", ARRAY),
  RECORDS_LIST("records list", ARRAY),

  LIST_OF_EXPRESSIONS("list of expressions"),
  ITERATIVE_EXPRESSION("iterative expression", EXPRESSION),
  SORTING_EXPRESSION("sorting expression", EXPRESSION),
  READ_ONLY_EXPRESSION("read-only expression", EXPRESSION),
  WRITE_ONLY_EXPRESSION("write-only expression", EXPRESSION),
  ASSIGN_EXPRESSION("assign expression", WRITE_ONLY_EXPRESSION);

  public static final Set<DQLDataType> ASSIGN_VALUE_TYPES = Set.of(NAMED_SUBQUERY_EXPRESSION, LIST_OF_EXPRESSIONS, WRITE_ONLY_EXPRESSION, ASSIGN_EXPRESSION);
  private final String name;
  private final Set<DQLDataType> parentTypes;

  public static final Set<DQLDataType> NUMERICAL_TYPES = Set.of(NUMBER, DURATION, TIMEFRAME, TIMESTAMP);
  public static final Set<DQLDataType> BOOLEAN_TYPES = Set.of(BOOLEAN);
  public static final Set<DQLDataType> COMPARABLE_TYPES = Set.of(NUMBER, DURATION, TIMEFRAME, TIMESTAMP, STRING);

  DQLDataType(String name) {
    this.name = name;
    this.parentTypes = Set.of();
  }

  DQLDataType(String name, DQLDataType ... parentTypes) {
    this.name = name;
    this.parentTypes = Set.of(parentTypes);
  }

  public static @NotNull Set<String> getTypes(@NotNull Collection<DQLDataType> types) {
    return types.stream().map(DQLDataType::getName).collect(Collectors.toSet());
  }

  public static @NotNull Set<String> getAllTypes(@NotNull Collection<DQLDataType> types) {
    List<DQLDataType> toProcess = new ArrayList<>(types);
    Set<String> result = new HashSet<>();
    while (!toProcess.isEmpty()) {
      DQLDataType dqlDataType = toProcess.removeFirst();
      toProcess.addAll(getImplementingTypes(dqlDataType));
      result.add(dqlDataType.getName());
    }
    return result;
  }

  public static @Nullable DQLDataType getType(DQLFunctionGroup group) {
    String groupName = group.getName().toLowerCase(); // contains "name-with-dashes-functions"
    String[] split = groupName.split("-");
    split[split.length - 1] = "function";
    DQLDataType result = DQLDataType.getType(String.join(" ", split));
    return result == ANY ? null : result;
  }

  public static @NotNull DQLDataType getType(String name) {
    return Arrays.stream(values()).filter(v -> v.name.equals(name)).findFirst().orElse(ANY);
  }

  public String getName() {
    return name;
  }

  public boolean satisfies(Set<DQLDataType> allowedValues) {
    if (this == ANY) {
      return true;
    }
    Set<DQLDataType> parentTypes = getParentTypes(Set.of(this));
    return DQLUtil.containsAny(parentTypes, allowedValues);
  }

  public static boolean doesNotSatisfy(Collection<DQLDataType> types, Collection<DQLDataType> allowedValues) {
    if (types.contains(ANY) || allowedValues.contains(ANY)) {
      return false;
    }
    Set<DQLDataType> parentTypes = getParentTypes(types);
    return !DQLUtil.containsAny(parentTypes, allowedValues);
  }

  private static Set<DQLDataType> getParentTypes(Collection<DQLDataType> types) {
    Set<DQLDataType> parents = new HashSet<>(types);
    List<DQLDataType> toCheck = new ArrayList<>(types);
    while (!toCheck.isEmpty()) {
      DQLDataType type = toCheck.removeFirst();
      toCheck.addAll(type.parentTypes);
      parents.addAll(type.parentTypes);
    }
    parents.add(ANY);
    return Collections.unmodifiableSet(parents);
  }

  private static Set<DQLDataType> getImplementingTypes(@Nullable DQLDataType type) {
    if (type == null) {
      return Set.of();
    }
    return Arrays.stream(values()).filter(v -> v.parentTypes.contains(type)).collect(Collectors.toSet());
  }
}
