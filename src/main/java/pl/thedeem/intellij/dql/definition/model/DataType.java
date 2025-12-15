package pl.thedeem.intellij.dql.definition.model;

import java.util.Objects;

public final class DataType {
    private String name;
    private String description;
    private Boolean experimental;

    public DataType() {
    }

    public DataType(
            String name,
            String description,
            Boolean experimental
    ) {
        this.name = name;
        this.description = description;
        this.experimental = experimental;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Boolean experimental() {
        return experimental;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExperimental(Boolean experimental) {
        this.experimental = experimental;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DataType) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.experimental, that.experimental);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, experimental);
    }

    @Override
    public String toString() {
        return "DataType[" +
                "name=" + name + ", " +
                "description=" + description + ", " +
                "experimental=" + experimental + ']';
    }

}
