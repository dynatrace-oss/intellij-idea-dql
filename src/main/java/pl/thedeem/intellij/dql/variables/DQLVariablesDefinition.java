package pl.thedeem.intellij.dql.variables;

import com.intellij.json.psi.*;
import org.jetbrains.annotations.Nullable;

public class DQLVariablesDefinition {

  public static @Nullable String getValue(JsonValue value) {
    if (value instanceof JsonStringLiteral literal) {
      return "\"" + literal.getValue() + "\"";
    } else if (value instanceof JsonNumberLiteral literal) {
      return String.valueOf(literal.getValue());
    } else if (value instanceof JsonBooleanLiteral literal) {
      return String.valueOf(literal.getValue());
    } else if (value instanceof JsonNullLiteral) {
      return "null";
    } else if (value instanceof JsonObject object) {
      StringBuilder builder = new StringBuilder("record(");
      boolean first = true;
      for (JsonProperty jsonProperty : object.getPropertyList()) {
        if (!first) {
          builder.append(", ");
        }
        first = false;
        builder.append(jsonProperty.getName()).append(" = ").append(getValue(jsonProperty.getValue()));
      }
      builder.append(")");
      return builder.toString();
    } else if (value instanceof JsonArray array) {
      StringBuilder builder = new StringBuilder("array(");
      boolean first = true;
      for (JsonValue jsonValue : array.getValueList()) {
        if (!first) {
          builder.append(", ");
        }
        first = false;
        builder.append(getValue(jsonValue));
      }
      builder.append(")");
      return builder.toString();
    }

    return null;
  }
}
