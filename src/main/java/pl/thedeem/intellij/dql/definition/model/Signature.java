package pl.thedeem.intellij.dql.definition.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class Signature {
    private List<Parameter> parameters;
    private List<String> outputs;
    private Boolean experimental;

    public Signature() {
    }

    public Signature(
            List<Parameter> parameters,
            List<String> outputs,
            Boolean experimental
    ) {
        this.parameters = parameters;
        this.outputs = outputs;
        this.experimental = experimental;
    }

    public List<Parameter> parameters() {
        return parameters;
    }

    public List<String> outputs() {
        return outputs;
    }

    public Boolean experimental() {
        return Boolean.TRUE.equals(experimental);
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void setOutputs(List<String> outputs) {
        this.outputs = outputs;
    }

    public void setExperimental(Boolean experimental) {
        this.experimental = experimental;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Signature) obj;
        return Objects.equals(this.parameters, that.parameters) &&
                Objects.equals(this.outputs, that.outputs) &&
                Objects.equals(this.experimental, that.experimental);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, outputs, experimental);
    }

    @Override
    public String toString() {
        return "Signature[" +
                "parameters=" + parameters + ", " +
                "outputs=" + outputs + ", " +
                "experimental=" + experimental + ']';
    }

    public @NotNull List<Parameter> requiredParameters() {
        List<Parameter> params = Objects.requireNonNullElse(parameters, List.of());
        return params.stream().filter(p -> !p.hidden() && Boolean.TRUE.equals(p.required())).toList();
    }
}
