package pl.thedeem.intellij.dql.definition.model;

import java.util.Objects;

public final class ParameterModifier {
    private String name;
    private String parameter;
    private String value;

    public ParameterModifier() {
    }

    public ParameterModifier(
            String name,
            String parameter,
            String value
    ) {
        this.name = name;
        this.parameter = parameter;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String parameter() {
        return parameter;
    }

    public String value() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ParameterModifier) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.parameter, that.parameter) &&
                Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parameter, value);
    }

    @Override
    public String toString() {
        return "ParameterModifier[" +
                "name=" + name + ", " +
                "parameter=" + parameter + ", " +
                "value=" + value + ']';
    }

}
