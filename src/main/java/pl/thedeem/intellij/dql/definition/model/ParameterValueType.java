package pl.thedeem.intellij.dql.definition.model;

import java.util.List;
import java.util.Objects;

public final class ParameterValueType {
    private String name;
    private String description;
    // custom
    private List<String> allowedFunctionCategories;

    public ParameterValueType() {
    }

    public ParameterValueType(
            String name,
            String description
    ) {
        this.name = name;
        this.description = description;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> allowedFunctionCategories() {
        return allowedFunctionCategories;
    }

    public void setAllowedFunctionCategories(List<String> allowedFunctionCategories) {
        this.allowedFunctionCategories = allowedFunctionCategories;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ParameterValueType) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }

    @Override
    public String toString() {
        return "ParameterValueType[" +
                "name=" + name + ", " +
                "description=" + description + ']';
    }
}
