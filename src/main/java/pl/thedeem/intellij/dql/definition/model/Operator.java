package pl.thedeem.intellij.dql.definition.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Operator {
    private String name;
    private String description;
    private String symbol;
    private List<Signature> signatures;
    // custom
    private Map<String, Map<String, String>> resultMapping;

    public Operator() {
    }

    public Operator(
            String name,
            String description,
            String symbol,
            List<Signature> signatures
    ) {
        this.name = name;
        this.description = description;
        this.symbol = symbol;
        this.signatures = signatures;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String symbol() {
        return symbol;
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

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setSignatures(List<Signature> signatures) {
        this.signatures = signatures;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Operator) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.symbol, that.symbol) &&
                Objects.equals(this.signatures, that.signatures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, symbol, signatures);
    }

    @Override
    public String toString() {
        return "Operator[" +
                "name=" + name + ", " +
                "description=" + description + ", " +
                "symbol=" + symbol + ", " +
                "signatures=" + signatures + ']';
    }

    public Map<String, Map<String, String>> resultMapping() {
        return Objects.requireNonNullElse(resultMapping, Map.of());
    }

    public void setResultMapping(Map<String, Map<String, String>> resultMapping) {
        this.resultMapping = resultMapping;
    }
}
