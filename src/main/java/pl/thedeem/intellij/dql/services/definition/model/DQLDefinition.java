package pl.thedeem.intellij.dql.services.definition.model;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.OptBoolean;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DQLDefinition {
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, DataType> dataTypes;
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, ParameterValueType> parameterValueTypes;
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, Command> commands;
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, FunctionCategory> functionCategories;
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, Function> functions;
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, Operator> operators;
    @JsonMerge(value = OptBoolean.TRUE)
    private Map<String, Operator> searchOperators;
    private List<String> dataSources;
    private List<String> stringDataTypes;
    private List<String> numericDataTypes;
    private List<String> stringParameterValueTypes;
    private List<String> dplParameterValueTypes;
    private List<String> executionBlockParameterValueTypes;
    private List<String> fieldIdentifierParameterValueTypes;
    private Map<String, String> filterNegations;

    public DQLDefinition() {
    }

    public DQLDefinition(
            Map<String, DataType> dataTypes,
            Map<String, ParameterValueType> parameterValueTypes,
            Map<String, Command> commands,
            Map<String, FunctionCategory> functionCategories,
            Map<String, Function> functions,
            Map<String, Operator> operators,
            Map<String, Operator> searchOperators,
            List<String> dataSources,
            List<String> stringDataTypes,
            List<String> numericDataTypes,
            List<String> stringParameterValueTypes,
            List<String> dplParameterValueTypes,
            List<String> executionBlockParameterValueTypes,
            List<String> fieldIdentifierParameterValueTypes,
            Map<String, String> filterNegations
    ) {
        this.dataTypes = dataTypes;
        this.parameterValueTypes = parameterValueTypes;
        this.commands = commands;
        this.functionCategories = functionCategories;
        this.functions = functions;
        this.operators = operators;
        this.searchOperators = searchOperators;
        this.dataSources = dataSources;
        this.stringDataTypes = stringDataTypes;
        this.numericDataTypes = numericDataTypes;
        this.stringParameterValueTypes = stringParameterValueTypes;
        this.dplParameterValueTypes = dplParameterValueTypes;
        this.executionBlockParameterValueTypes = executionBlockParameterValueTypes;
        this.fieldIdentifierParameterValueTypes = fieldIdentifierParameterValueTypes;
        this.filterNegations = filterNegations;
    }

    public static @NotNull DQLDefinition empty() {
        return new DQLDefinition(
                Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                Map.of()
        );
    }

    public Map<String, DataType> dataTypes() {
        return dataTypes;
    }

    public Map<String, ParameterValueType> parameterValueTypes() {
        return parameterValueTypes;
    }

    public Map<String, Command> commands() {
        return commands;
    }

    public Map<String, FunctionCategory> functionCategories() {
        return functionCategories;
    }

    public Map<String, Function> functions() {
        return functions;
    }

    public Map<String, Operator> operators() {
        return operators;
    }

    public Map<String, Operator> searchOperators() {
        return searchOperators;
    }

    public void setDataTypes(Map<String, DataType> dataTypes) {
        this.dataTypes = dataTypes;
    }

    public void setParameterValueTypes(Map<String, ParameterValueType> parameterValueTypes) {
        this.parameterValueTypes = parameterValueTypes;
    }

    public void setCommands(Map<String, Command> commands) {
        this.commands = commands;
    }

    public void setFunctionCategories(Map<String, FunctionCategory> functionCategories) {
        this.functionCategories = functionCategories;
    }

    public void setFunctions(Map<String, Function> functions) {
        this.functions = functions;
    }

    public void setOperators(Map<String, Operator> operators) {
        this.operators = operators;
    }

    public void setSearchOperators(Map<String, Operator> searchOperators) {
        this.searchOperators = searchOperators;
    }


    public List<String> dataSources() {
        return Objects.requireNonNullElseGet(dataSources, List::of);
    }

    public void setDataSources(List<String> dataSources) {
        this.dataSources = Objects.requireNonNullElseGet(dataSources, List::of);
    }

    public List<String> stringDataTypes() {
        return Objects.requireNonNullElseGet(stringDataTypes, List::of);
    }

    public void setStringDataTypes(List<String> stringDataTypes) {
        this.stringDataTypes = stringDataTypes;
    }

    public List<String> numericDataTypes() {
        return Objects.requireNonNullElseGet(numericDataTypes, List::of);
    }

    public void setNumericDataTypes(List<String> numericDataTypes) {
        this.numericDataTypes = numericDataTypes;
    }

    public List<String> dplParameterValueTypes() {
        return Objects.requireNonNullElseGet(dplParameterValueTypes, List::of);
    }

    public void setDplParameterValueTypes(List<String> dplParameterValueTypes) {
        this.dplParameterValueTypes = dplParameterValueTypes;
    }

    public List<String> executionBlockParameterValueTypes() {
        return Objects.requireNonNullElseGet(executionBlockParameterValueTypes, List::of);
    }

    public void setExecutionBlockParameterValueTypes(List<String> executionBlockParameterValueTypes) {
        this.executionBlockParameterValueTypes = executionBlockParameterValueTypes;
    }

    public List<String> fieldIdentifierParameterValueTypes() {
        return Objects.requireNonNullElseGet(fieldIdentifierParameterValueTypes, List::of);
    }

    public void setFieldIdentifierParameterValueTypes(List<String> fieldIdentifierParameterValueTypes) {
        this.fieldIdentifierParameterValueTypes = fieldIdentifierParameterValueTypes;
    }

    public Map<String, String> filterNegations() {
        return Objects.requireNonNullElseGet(filterNegations, Map::of);
    }

    public void setFilterNegations(Map<String, String> filterNegations) {
        this.filterNegations = filterNegations;
    }

    public List<String> stringParameterValueTypes() {
        return stringParameterValueTypes;
    }

    public void setStringParameterValueTypes(List<String> stringParameterValueTypes) {
        this.stringParameterValueTypes = stringParameterValueTypes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DQLDefinition) obj;
        return Objects.equals(this.dataTypes, that.dataTypes) &&
                Objects.equals(this.parameterValueTypes, that.parameterValueTypes) &&
                Objects.equals(this.commands, that.commands) &&
                Objects.equals(this.functionCategories, that.functionCategories) &&
                Objects.equals(this.functions, that.functions) &&
                Objects.equals(this.operators, that.operators) &&
                Objects.equals(this.searchOperators, that.searchOperators) &&
                Objects.equals(this.dataSources, that.dataSources) &&
                Objects.equals(this.stringDataTypes, that.stringDataTypes) &&
                Objects.equals(this.numericDataTypes, that.numericDataTypes) &&
                Objects.equals(this.stringParameterValueTypes, that.stringParameterValueTypes) &&
                Objects.equals(this.dplParameterValueTypes, that.dplParameterValueTypes) &&
                Objects.equals(this.fieldIdentifierParameterValueTypes, that.fieldIdentifierParameterValueTypes) &&
                Objects.equals(this.filterNegations, that.filterNegations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                dataTypes, parameterValueTypes, commands, functionCategories,
                functions, operators, searchOperators, dataSources,
                stringDataTypes, numericDataTypes, stringParameterValueTypes, dplParameterValueTypes,
                fieldIdentifierParameterValueTypes, filterNegations
        );
    }

    @Override
    public String toString() {
        return "DQLDefinition[" +
                "dataTypes=" + dataTypes + ", " +
                "parameterValueTypes=" + parameterValueTypes + ", " +
                "commands=" + commands + ", " +
                "functionCategories=" + functionCategories + ", " +
                "functions=" + functions + ", " +
                "operators=" + operators + ", " +
                "searchOperators=" + searchOperators + ", " +
                "dataSources=" + dataSources + ", " +
                "stringDataTypes=" + stringDataTypes + ", " +
                "numericDataTypes=" + numericDataTypes + ", " +
                "stringParameterValueTypes=" + stringParameterValueTypes + ", " +
                "dplParameterValueTypes=" + dplParameterValueTypes + ", " +
                "fieldIdentifierParameterValueTypes=" + fieldIdentifierParameterValueTypes + ", " +
                "filterNegations=" + filterNegations
                + ']';
    }
}
