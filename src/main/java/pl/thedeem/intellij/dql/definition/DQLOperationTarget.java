package pl.thedeem.intellij.dql.definition;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.psi.DQLTypes;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.*;
import java.util.stream.Collectors;

public class DQLOperationTarget {
    public static final String ADDITION = "addition";
    public static final String SUBTRACT = "subtract";
    public static final String MULTIPLY = "multiply";
    public static final String DIVIDE = "divide";
    public static final String MODULO = "modulo";
    public static final String EQUALITY = "equality";
    public static final String COMPARISON = "comparison";
    public static final List<String> ALL = List.of(ADDITION, SUBTRACT, MULTIPLY, DIVIDE, MODULO, EQUALITY, COMPARISON);

    public String operator;
    public String defaultValue;
    public Map<String, Map<String, String>> mapping;

    private Set<IElementType> operatorTypes;
    private Map<DQLDataType, Map<DQLDataType, DQLDataType>> dataTypeMap;
    private DQLDataType defaultType;

    public void initialize() {
        this.operatorTypes = calculateOperatorType();
        this.dataTypeMap = calculateDataTypes();
        this.defaultType = StringUtil.isNotEmpty(defaultValue) ? DQLDataType.getType(defaultValue) : DQLDataType.UNKNOWN;
    }

    public Set<IElementType> getOperatorType() {
        return operatorTypes;
    }

    public Set<DQLDataType> getMapping(@NotNull Set<DQLDataType> leftSide, @NotNull Set<DQLDataType> rightSide) {
        if (leftSide.contains(DQLDataType.ANY) || rightSide.contains(DQLDataType.ANY)) {
            return Set.of(DQLDataType.ANY);
        }
        Set<DQLDataType> result = new HashSet<>();

        Set<DQLDataType> matchingLeft = findDataType(leftSide, dataTypeMap.keySet());
        if (matchingLeft.isEmpty()) {
            result.add(defaultType);
        }
        else {
            boolean found = false;
            for (DQLDataType leftType : matchingLeft) {
                Map<DQLDataType, DQLDataType> available = dataTypeMap.get(leftType);
                Set<DQLDataType> matchingRight = findDataType(rightSide, available.keySet());
                if (!matchingRight.isEmpty()) {
                    found = true;
                    for (DQLDataType resultType : matchingRight) {
                         result.add(available.get(resultType));
                    }
                }
            }
            if (!found) {
                result.add(defaultType);
            }
        }

        return result;
    }

    public @NotNull Map<BaseElement, Set<DQLDataType>> getInvalidSides(BaseElement left, BaseElement right) {
        Map<BaseElement, Set<DQLDataType>> invalidSides = new HashMap<>();

        Set<DQLDataType> leftReturns = left.getDataType();

        Set<DQLDataType> matchingLeft = findDataType(leftReturns, dataTypeMap.keySet());
        if (matchingLeft.isEmpty()) {
            invalidSides.put(left, dataTypeMap.keySet());
        }
        else {
            boolean found = false;
            Set<DQLDataType> rightReturns = right.getDataType();
            for (DQLDataType leftType : matchingLeft) {
                Map<DQLDataType, DQLDataType> available = dataTypeMap.get(leftType);
                Set<DQLDataType> matchingRight = findDataType(rightReturns, available.keySet());
                if (!matchingRight.isEmpty()) {
                    break;
                }
            }
            if (!found) {
                invalidSides.put(right, matchingLeft.stream().flatMap(m -> dataTypeMap.get(m).keySet().stream()).collect(Collectors.toSet()));
            }
        }

        return invalidSides;
    }

    private Set<IElementType> calculateOperatorType() {
        return switch (operator) {
            case ADDITION -> Set.of(DQLTypes.ADD);
            case SUBTRACT -> Set.of(DQLTypes.SUBTRACT);
            case MULTIPLY -> Set.of(DQLTypes.MULTIPLY);
            case DIVIDE -> Set.of(DQLTypes.DIVIDE);
            case MODULO -> Set.of(DQLTypes.MODULO);
            case EQUALITY -> Set.of(DQLTypes.EQUALS, DQLTypes.NOT_EQUALS);
            case COMPARISON -> Set.of(DQLTypes.GREATER_THAN, DQLTypes.GREATER_OR_EQUALS_THAN, DQLTypes.LESSER_THAN, DQLTypes.LESSER_OR_EQUALS_THAN);
            default -> Set.of();
        };
    }

    private Map<DQLDataType, Map<DQLDataType, DQLDataType>> calculateDataTypes() {
        if (mapping == null) {
            return Map.of();
        }
        Map<DQLDataType, Map<DQLDataType, DQLDataType>> result = new HashMap<>(mapping.size());
        for (Map.Entry<String, Map<String, String>> main : mapping.entrySet()) {
            DQLDataType rightType = DQLDataType.getType(main.getKey());
            Map<DQLDataType, DQLDataType> inside = new HashMap<>(main.getValue().size());
            result.put(rightType, inside);
            for (Map.Entry<String, String> map : main.getValue().entrySet()) {
                DQLDataType leftType = DQLDataType.getType(map.getKey());
                DQLDataType resultType = DQLDataType.getType(map.getValue());
                inside.put(leftType, resultType);
            }
        }
        return result;
    }

    private @NotNull Set<DQLDataType> findDataType(@NotNull Set<DQLDataType> returns, @NotNull Set<DQLDataType> available) {
        Set<DQLDataType> result = new HashSet<>();
        for (DQLDataType availableType : available) {
            for (DQLDataType returnedType : returns) {
                if (returnedType.satisfies(Set.of(availableType))) {
                    result.add(availableType);
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }
}
