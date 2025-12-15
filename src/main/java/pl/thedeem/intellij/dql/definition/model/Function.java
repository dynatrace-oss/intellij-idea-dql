package pl.thedeem.intellij.dql.definition.model;

import java.util.List;
import java.util.Objects;

public final class Function {
    private String name;
    private String description;
    private String category;
    private String synopsis;
    private Boolean experimental;
    private Boolean deprecated;
    private List<Signature> signatures;

    public Function() {
    }

    public Function(
            String name,
            String description,
            String category,
            String synopsis,
            Boolean experimental,
            Boolean deprecated,
            List<Signature> signatures
    ) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.synopsis = synopsis;
        this.experimental = experimental;
        this.deprecated = deprecated;
        this.signatures = signatures;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String category() {
        return category;
    }

    public String synopsis() {
        return synopsis;
    }

    public Boolean experimental() {
        return Boolean.TRUE.equals(experimental);
    }

    public Boolean deprecated() {
        return Boolean.TRUE.equals(deprecated);
    }

    public List<Signature> signatures() {
        return signatures;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public void setExperimental(Boolean experimental) {
        this.experimental = experimental;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public void setSignatures(List<Signature> signatures) {
        this.signatures = signatures;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Function) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.category, that.category) &&
                Objects.equals(this.synopsis, that.synopsis) &&
                Objects.equals(this.experimental, that.experimental) &&
                Objects.equals(this.deprecated, that.deprecated) &&
                Objects.equals(this.signatures, that.signatures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, category, synopsis, experimental, deprecated, signatures);
    }

    @Override
    public String toString() {
        return "Function[" +
                "name=" + name + ", " +
                "description=" + description + ", " +
                "category=" + category + ", " +
                "synopsis=" + synopsis + ", " +
                "experimental=" + experimental + ", " +
                "deprecated=" + deprecated + ", " +
                "signatures=" + signatures + ']';
    }

    public List<Parameter> requiredParameters() {
        if (this.signatures == null || this.signatures.isEmpty()) {
            return List.of();
        }
        return this.signatures.getFirst().parameters().stream().filter(p -> !p.hidden() && p.required()).toList();
    }
}
