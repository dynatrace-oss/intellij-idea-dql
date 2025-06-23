package pl.thedeem.intellij.dql.definition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.impl.ExpressionOperatorImpl;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DQLOperationsLoader {
    private static final String OPERATOR_DEFINITION_FILE = "/dql/operators/%s.json";

    private static final Map<IElementType, DQLOperationTarget> MAPPING = loadOperators();

    private static Map<IElementType, DQLOperationTarget> loadOperators() {
        Map<IElementType, DQLOperationTarget> result = new HashMap<>();
        for (String operator : DQLOperationTarget.ALL) {
            String filePath = String.format(OPERATOR_DEFINITION_FILE, operator);
            try (InputStream inputStream = DQLFunctionsLoader.class.getResourceAsStream(filePath)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Operator definitions file not found: " + filePath);
                }
                ObjectMapper mapper = new ObjectMapper();
                TypeReference<DQLOperationTarget> typeRef = new TypeReference<>() {
                };
                DQLOperationTarget r = mapper.readValue(inputStream, typeRef);
                r.initialize();
                for (IElementType type : r.getOperatorType()) {
                    result.put(type, r);
                }
            } catch (IOException ignored) {
                System.err.println("Failed to load function operator from " + filePath);
            }
        }
        return result;
    }

    public static @NotNull Set<DQLDataType> getResultType(@Nullable ExpressionOperatorImpl operator, @Nullable DQLExpression left, @Nullable DQLExpression right) {
        if (operator != null && left instanceof BaseTypedElement leftEl && right instanceof BaseTypedElement rightEl) {
            return getResultType(operator.getNodeType(), leftEl.getDataType(), rightEl.getDataType());
        }
        return Set.of(DQLDataType.UNKNOWN);
    }

    public static @Nullable DQLOperationTarget getTargetType(@Nullable ExpressionOperatorImpl operator) {
        if (operator != null) {
            return MAPPING.get(operator.getNodeType());
        }
        return null;
    }

    public static @NotNull Set<DQLDataType> getResultType(@NotNull IElementType operator, @NotNull Set<DQLDataType> left, @NotNull Set<DQLDataType> right) {
        DQLOperationTarget target = MAPPING.get(operator);
        if (target == null) {
            return Set.of(DQLDataType.UNKNOWN);
        }
        return target.getMapping(left, right);
    }
}
