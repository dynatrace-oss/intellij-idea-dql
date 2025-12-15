package pl.thedeem.intellij.dql.definition.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Parameter {
    private String name;
    private String description;
    private Boolean required;
    private Boolean requiresName;
    private Boolean experimental;
    private Boolean variadic;
    private String assignmentSupport;
    private List<String> valueTypes;
    private List<String> parameterValueTypes;
    private List<String> allowedEnumValues;
    private String defaultValue;
    private List<ParameterModifier> modifiers;
    private List<String> aliases;
    private String minValue;
    private String maxValue;
    // custom
    private List<String> excludes;
    private Boolean allowsName = true;
    private Boolean hidden = false;

    public Parameter() {
    }

    public Parameter(
            String name,
            String description,
            Boolean required,
            Boolean requiresName,
            Boolean experimental,
            Boolean variadic,
            String assignmentSupport,
            List<String> valueTypes,
            List<String> parameterValueTypes,
            List<String> allowedEnumValues,
            String defaultValue,
            List<ParameterModifier> modifiers,
            List<String> aliases,
            String minValue,
            String maxValue
    ) {
        this.name = name;
        this.description = description;
        this.required = required;
        this.requiresName = requiresName;
        this.experimental = experimental;
        this.variadic = variadic;
        this.assignmentSupport = assignmentSupport;
        this.valueTypes = valueTypes;
        this.parameterValueTypes = parameterValueTypes;
        this.allowedEnumValues = allowedEnumValues;
        this.defaultValue = defaultValue;
        this.modifiers = modifiers;
        this.aliases = aliases;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Boolean required() {
        return required;
    }

    public Boolean requiresName() {
        return Boolean.TRUE.equals(requiresName);
    }

    public Boolean experimental() {
        return Boolean.TRUE.equals(experimental);
    }

    public Boolean variadic() {
        return Boolean.TRUE.equals(variadic);
    }

    public String assignmentSupport() {
        return assignmentSupport;
    }

    public List<String> valueTypes() {
        return valueTypes;
    }

    public List<String> parameterValueTypes() {
        return parameterValueTypes;
    }

    public List<String> allowedEnumValues() {
        return allowedEnumValues;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public List<ParameterModifier> modifiers() {
        return modifiers;
    }

    public List<String> aliases() {
        return aliases;
    }

    public String minValue() {
        return minValue;
    }

    public String maxValue() {
        return maxValue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public void setRequiresName(Boolean requiresName) {
        this.requiresName = requiresName;
    }

    public void setExperimental(Boolean experimental) {
        this.experimental = experimental;
    }

    public void setVariadic(Boolean variadic) {
        this.variadic = variadic;
    }

    public void setAssignmentSupport(String assignmentSupport) {
        this.assignmentSupport = assignmentSupport;
    }

    public void setValueTypes(List<String> valueTypes) {
        this.valueTypes = valueTypes;
    }

    public void setParameterValueTypes(List<String> parameterValueTypes) {
        this.parameterValueTypes = parameterValueTypes;
    }

    public void setAllowedEnumValues(List<String> allowedEnumValues) {
        this.allowedEnumValues = allowedEnumValues;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setModifiers(List<ParameterModifier> modifiers) {
        this.modifiers = modifiers;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public List<String> excludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public void setAllowsName(Boolean allowsName) {
        this.allowsName = allowsName;
    }

    public Boolean hidden() {
        return Boolean.TRUE.equals(hidden);
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Parameter) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.required, that.required) &&
                Objects.equals(this.requiresName, that.requiresName) &&
                Objects.equals(this.experimental, that.experimental) &&
                Objects.equals(this.variadic, that.variadic) &&
                Objects.equals(this.assignmentSupport, that.assignmentSupport) &&
                Objects.equals(this.valueTypes, that.valueTypes) &&
                Objects.equals(this.parameterValueTypes, that.parameterValueTypes) &&
                Objects.equals(this.allowedEnumValues, that.allowedEnumValues) &&
                Objects.equals(this.defaultValue, that.defaultValue) &&
                Objects.equals(this.modifiers, that.modifiers) &&
                Objects.equals(this.aliases, that.aliases) &&
                Objects.equals(this.minValue, that.minValue) &&
                Objects.equals(this.maxValue, that.maxValue) &&
                Objects.equals(this.excludes, that.excludes) &&
                Objects.equals(this.allowsName, that.allowsName) &&
                Objects.equals(this.hidden, that.hidden);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, required, requiresName, experimental, variadic,
                assignmentSupport, valueTypes, parameterValueTypes, allowedEnumValues, defaultValue,
                modifiers, aliases, minValue, maxValue, excludes, allowsName, hidden);
    }

    public boolean allowsName() {
        return this.allowsName;
    }

    public boolean allowsSorting() {
        return modifiers != null && modifiers.stream().anyMatch(m -> Set.of("asc", "desc").contains(m.name()));
    }
}
