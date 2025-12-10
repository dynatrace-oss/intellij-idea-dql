package pl.thedeem.intellij.dql.definition.model;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pl.thedeem.intellij.dql.definition.model.tools.ParameterListMergingDeserializer;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class Command {
    private String name;
    private String description;
    private String synopsis;
    private Boolean experimental;
    @JsonMerge(value = OptBoolean.TRUE)
    @JsonDeserialize(using = ParameterListMergingDeserializer.class)
    private List<Parameter> parameters;
    // custom
    private String category = "extension";
    private List<String> oneOfRequired;

    public Command() {
    }

    public Command(
            String name,
            String description,
            String synopsis,
            Boolean experimental,
            List<Parameter> parameters,
            // custom
            String category
    ) {
        this.name = name;
        this.description = description;
        this.synopsis = synopsis;
        this.experimental = experimental;
        this.parameters = parameters;
        this.category = category;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String synopsis() {
        return synopsis;
    }

    public Boolean experimental() {
        return Boolean.TRUE.equals(experimental);
    }

    public List<Parameter> parameters() {
        return Objects.requireNonNullElse(parameters, List.of());
    }

    public String category() {
        return category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public void setExperimental(Boolean experimental) {
        this.experimental = experimental;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> oneOfRequired() {
        return oneOfRequired;
    }

    public void setOneOfRequired(List<String> oneOfRequired) {
        this.oneOfRequired = oneOfRequired;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Command) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.synopsis, that.synopsis) &&
                Objects.equals(this.experimental, that.experimental) &&
                Objects.equals(this.parameters, that.parameters) &&
                Objects.equals(this.category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, synopsis, experimental, parameters, category);
    }

    @Override
    public String toString() {
        return "Command[" +
                "name=" + name + ", " +
                "description=" + description + ", " +
                "synopsis=" + synopsis + ", " +
                "experimental=" + experimental + ", " +
                "parameters=" + parameters + ", " +
                "category=" + category + ']';
    }

    public Collection<Parameter> requiredParameters() {
        List<Parameter> params = Objects.requireNonNullElse(parameters, List.of());
        return params.stream().filter((Parameter p) -> !p.hidden() && Boolean.TRUE.equals(p.required())).toList();
    }
}
