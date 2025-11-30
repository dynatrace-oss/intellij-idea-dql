package pl.thedeem.intellij.dql.definition;

import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DQLParameterDefinition {
    public String name;
    public String description;
    public boolean required;
    public boolean repetitive;
    public Boolean nameAllowed;
    public Boolean allowsDuplicates;
    public Boolean constant;
    public boolean singleUnnamed;
    public List<String> type;
    public List<String> disallows;
    public String defaultValue;
    public List<String> enumValues;
    public List<String> suggested;

    @Override
    public String toString() {
        return this.name;
    }

    public boolean canBeNamed() {
        return !Boolean.FALSE.equals(nameAllowed);
    }

    public boolean satisfies(DQLDataType type) {
        return this.type.contains(type.getName());
    }

    public boolean allowsDuplicates() {
        return Boolean.TRUE.equals(allowsDuplicates);
    }

    public Set<DQLDataType> getDQLTypes() {
        return this.type != null ? this.type.stream().map(DQLDataType::getType).collect(Collectors.toSet()) : Set.of();
    }

    public boolean allowsDataAccess() {
        return !Boolean.TRUE.equals(constant);
    }

    public boolean isRepetitive() {
        return repetitive;
    }

    public List<String> getDisallowedParameters() {
        return disallows == null ? Collections.emptyList() : disallows;
    }

    public boolean isEnum() {
        return enumValues != null && !enumValues.isEmpty();
    }
}
