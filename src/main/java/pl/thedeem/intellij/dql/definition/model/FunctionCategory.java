package pl.thedeem.intellij.dql.definition.model;

import java.util.Objects;

public final class FunctionCategory {
    private String name;
    private String description;

    public FunctionCategory() {
    }

    public FunctionCategory(
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FunctionCategory) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }

    @Override
    public String toString() {
        return "FunctionCategory[" +
                "name=" + name + ", " +
                "description=" + description + ']';
    }

}
